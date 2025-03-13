package ru.students.forumservicediplomproject.service;

import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import ru.students.forumservicediplomproject.dto.PostDto;
import ru.students.forumservicediplomproject.entity.Post;
import ru.students.forumservicediplomproject.entity.Thread;
import ru.students.forumservicediplomproject.exeption.HashAlreadyRegisteredException;
import ru.students.forumservicediplomproject.exeption.ResourceNotFoundException;
import ru.students.forumservicediplomproject.exeption.TrackerServiceException;
import ru.students.forumservicediplomproject.repository.PeersRepository;
import ru.students.forumservicediplomproject.repository.PostRepository;

import java.net.URI;
import java.sql.Timestamp;
import java.util.*;

@Slf4j
@Service
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final UserService userService;
    private final PeersRepository peersRepository;
    private final MessageService messageService;

    public PostServiceImpl(PostRepository postRepository, UserService userService,
                           PeersRepository peersRepository, MessageService messageService) {
        this.postRepository = postRepository;
        this.userService = userService;
        this.peersRepository = peersRepository;
        this.messageService = messageService;
    }

    /**
     * @param torrentFile Загруженный пользователем торрент файл
     * @param postDto     Заполненная пользователем форма создания темы
     * @param thread      Идентификатор ветки
     * @param forumId     Идентификатор форума
     * @return Хеш сумма раздачи
     */
    @Override
    public long savePost(MultipartFile torrentFile,
                         PostDto postDto, Thread thread, long forumId) {

        String hash = getHash(torrentFile);
        if (postRepository.existsByHashInfo(hash)) {
            log.error("Тема с таким хешем {} уже существует", hash);
            throw new HashAlreadyRegisteredException("Тема с таким хешем %s уже существует".formatted(hash));
        }
        Post post = new Post();
        post.setTitle(postDto.getTitle());
        post.setCreatedBy(userService.getCurrentUserCredentials());
        post.setHashInfo(hash);
        post.setCreationDate(new Timestamp(new Date().getTime()));
        post.setPostStatus(Post.Status.NEW);
        post.setThread(thread);

        postRepository.save(post);

        try {
            registerNewTorrent(torrentFile, post);
        } catch (HttpClientErrorException.Conflict e) {
            log.error("На трекере уже есть такой торрент. Откат сохранения поста");
            postRepository.delete(post);
            throw new HashAlreadyRegisteredException("На трекере уже есть такой торрент", e);
        } catch (RestClientException e) {
            log.error("При регистрации раздачи на трекере произошла ошибка. Откат сохранения поста");
            postRepository.delete(post);
            throw new TrackerServiceException("При регистрации раздачи на трекере произошла ошибка", e);
        }

        return post.getPostId();
    }

    private String getHash(MultipartFile torrentFile) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("torrentFile", torrentFile.getResource());

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        String uri = "http://localhost:8081/getHash";
        URI uri1 = UriComponentsBuilder.fromUriString(uri)
                .build().toUri();
        ResponseEntity<String> response = restTemplate.postForEntity(uri1, requestEntity, String.class);

        if (response.getBody().length() != 40) {
            log.error("Returned hashInfo not 40 digits, actual {}", response.getBody().length());
            throw new RuntimeException("Returned hashInfo not 40 digits, actual %d".formatted(response.getBody().length()));
        }
        return response.getBody();
    }

    @Override
    public void changePost(Post post) {
        if (!postRepository.existsById(post.getPostId())) {
            log.error("Trying edit not existing post {}", post.getPostId());
            return;
        }
        postRepository.save(post);
    }

    @Override
    public void changePostStatus(String hash, String status) {
        log.info("Received new status {} for post with hash {}", status, hash);
        Post post = getPostByHashInfo(hash);
        if (status.equalsIgnoreCase(Post.Status.INACTIVE.name())) {
            post.setPostStatus(Post.Status.INACTIVE);
        } else if (status.equalsIgnoreCase(Post.Status.ACTIVE.name())) {
            post.setPostStatus(Post.Status.ACTIVE);
        } else if (status.equalsIgnoreCase(Post.Status.ARCHIVE.name())) {
            post.setPostStatus(Post.Status.ARCHIVE);
        } else {
            log.warn("Received unknown status {} for {}", status, hash);
            return;
        }
        log.debug("Set status {} for post with id {}", status, post.getPostId());
        changePost(post);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, noRollbackFor = Exception.class)
    public Post getPostById(long postId) {
        Optional<Post> optionalPost = postRepository.findById(postId);
        if (optionalPost.isEmpty()) {
            log.error("Пост с id {} не найден", postId);
            throw new ResourceNotFoundException("Пост не найден! Id %d".formatted(postId));
        }
        Post post = optionalPost.get();
        post.setTotalMessagesInPost(countTotalMessagesInPost(post));
        post.setLastMessageInPost(messageService.getLastMessageByPost(post));
        return post;
    }

    @Override
    public Post getPostByHashInfo(String hash) {
        return postRepository.findByHashInfo(hash);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, noRollbackFor = Exception.class)
    public void deleteAllByThread(Thread thread) {
        List<Post> posts = getAllPostsByThread(thread);
        posts.forEach(this::deletePost);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, noRollbackFor = Exception.class)
    public void deletePost(Post post) {
        RestTemplate restTemplate = new RestTemplate();
        URI uri = UriComponentsBuilder.fromUriString("http://localhost:8081/deleteTorrent/{hash}")
                .build(Collections.singletonMap("hash", post.getHashInfo()));
        try {
            restTemplate.delete(uri);
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("При удалении темы трекер не смог найти раздачу с хешем {}. Тема удалена", post.getHashInfo());
        }

        messageService.deleteAllMessagesByPost(post);

        peersRepository.deleteAllByPost(post);

        postRepository.delete(post);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, noRollbackFor = Exception.class)
    public void updatePost(Post post) {
        postRepository.save(post);
    }

    @Override
    public List<Post> getAllPosts() {
        List<Post> postList = postRepository.findAll();
        for (Post post:postList) {
            post.setTotalMessagesInPost(countTotalMessagesInPost(post));
            post.setLastMessageInPost(messageService.getLastMessageByPost(post));
        }
        return postList;
    }

    @Nullable
    @Override
    public Post getLastCreatedPost() {
        List<Post> postList = postRepository.findAllByCreationDateBeforeOrderByCreationDateDesc(new Timestamp(new Date().getTime() + 1000));
        if (postList.isEmpty()) {
            return null;
        }
        return postList.get(0);
    }

    @Override
    public List<Post> getAllPostsByThread(Thread thread) {
        return postRepository.findByThread(thread);
    }

    @Override
    public Long countPostsByThread(Thread threadId) {
        return postRepository.countAllByThread(threadId);
    }

    @Override
    public Long countTotalMessagesInPostsByThread(Thread threadId) {
        long total = 0;
        List<Post> allPostsByThread = getAllPostsByThread(threadId);
        for (Post post:allPostsByThread) {
            total += messageService.countMessagesByPost(post);
        }
        return total;
    }

    @Override
    public Long countTotalMessagesInPost(Post post) {
        return messageService.countMessagesByPost(post);
    }

    @Override
    public long getCountOfAllPosts() {
        return postRepository.count();
    }

    /**
     * @param torrentFile Загруженный пользователем торрент файл
     */
    @Override
    public void registerNewTorrent(MultipartFile torrentFile, Post post) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("torrentFile", torrentFile.getResource());
        body.add("name", "%s [post-%d]".formatted(post.getTitle(), post.getPostId()));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        String uri = "http://localhost:8081/register";
        URI uri1 = UriComponentsBuilder.fromUriString(uri)
                .build().toUri();
        ResponseEntity<String> response;
        try {
            response = restTemplate.postForEntity(uri1, requestEntity, String.class);
        } catch (HttpClientErrorException.Conflict e) {
            throw new RuntimeException(e);
        }

        response.getBody();
    }

    @Override
    public void approvePost(long postId) {
        Optional<Post> optionalPost = postRepository.findById(postId);
        if (optionalPost.isEmpty()) {
            log.error("Произошла ошибка при проверке поста! Пост не найден {}", postId);
            throw new RuntimeException("Произошла ошибка при проверке поста! Пост не найден");
        }
        Post post = optionalPost.get();

        HttpHeaders headers = new HttpHeaders();

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("hashInfo", post.getHashInfo());

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        String uri = "http://localhost:8081/announce";
        URI uri1 = UriComponentsBuilder.fromUriString(uri)
                .build().toUri();
        restTemplate.postForEntity(uri1, requestEntity, String.class);


        post.setPostStatus(Post.Status.APPROVED);

        postRepository.save(post);
    }

    /**
     * @param postId id темы
     * @return List, где 0 - имя файла, 1 - байты файла
     */
    @Transactional(propagation = Propagation.REQUIRED, noRollbackFor = Exception.class)
    @Nullable
    @Override
    //TODO: тут надо бы не List, а что-то нормальное
    public List getTorrentFileForDownload(long postId) {
        Post post = getPostById(postId);

        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8081/download/{hashInfo}";
        Map<String, String> params = Collections.singletonMap("hashInfo", post.getHashInfo());

        ResponseEntity response = restTemplate.getForEntity(url, byte[].class, params);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            List o = new ArrayList<>(2);
            o.add(response.getHeaders().getContentDisposition().getFilename());
            o.add(response.getBody());

            post.setCountOfDownloads(post.getCountOfDownloads() + 1);
            updatePost(post);
            return o;
        } else {
            return null;
        }

    }

    @Override
    public List<Post> getPostsWithNewStatus() {
        return postRepository.findAllByPostStatus(Post.Status.NEW);
    }
}
