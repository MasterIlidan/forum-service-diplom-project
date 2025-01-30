package ru.students.forumservicediplomproject.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import ru.students.forumservicediplomproject.dto.MessageDto;
import ru.students.forumservicediplomproject.dto.PostDto;
import ru.students.forumservicediplomproject.entity.Post;
import ru.students.forumservicediplomproject.entity.Thread;
import ru.students.forumservicediplomproject.repository.PeersRepository;
import ru.students.forumservicediplomproject.repository.PostRepository;
import ru.students.forumservicediplomproject.entity.Peers;

import java.net.URI;
import java.sql.Timestamp;
import java.util.*;

@Slf4j
@Service
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final UserService userService;
    private final ThreadService threadService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private PeersRepository peersRepository;
    @Autowired
    private PeersService peersService;

    public PostServiceImpl(PostRepository postRepository, UserService userService, ThreadService threadService) {
        this.postRepository = postRepository;
        this.userService = userService;
        this.threadService = threadService;
    }

    /**
     *
     * @param torrentFile
     * Загруженный пользователем торрент файл
     * @param postDto
     * Заполненная пользователем форма создания темы
     * @param threadId
     * Идентификатор ветки
     * @param forumId
     * Идентификатор форума
     * @return
     * Хеш сумма раздачи
     */
    @Override
    public long savePost(MultipartFile torrentFile, PostDto postDto, long threadId, long forumId) {

        String hash = registerNewTorrent(torrentFile);

        Post post = new Post();
        post.setTitle(postDto.getTitle());
        post.setCreatedBy(userService.getCurrentUserCredentials());
        post.setHashInfo(hash);
        post.setCreationDate(new Timestamp(new Date().getTime()));
        Optional<Thread> thread = threadService.getThreadById(threadId);

        if (thread.isPresent()) {
            post.setThread(thread.get());
        } else {
            throw new RuntimeException("Ветка поста не найдена! PostId %s ForumId %s".formatted(threadId, forumId));
        }

        postRepository.save(post);

        MessageDto messageDto = new MessageDto();
        messageDto.setMessageBody(postDto.getMessageBody());
        messageService.saveMessage(messageDto, post.getPostId());

        return post.getPostId();
    }

    @Override
    public Optional<Post> getPostById(long postId) {
        return postRepository.findById(postId);
    }

    @Override
    public Post getPostByHashInfo(String hash) {
        return postRepository.findByHashInfo(hash);
    }

    @Override
    public void deletePost(Post post) {
        postRepository.delete(post);
    }

    @Override
    public void updatePost(Post post) {
        postRepository.save(post);
    }

    @Override
    public List<Post> getAllPosts() {

        return postRepository.findAll();
    }
    @Override
    public HashMap<Post, Peers> getPeers(List<Post> postList) {

        HashMap<Post, Peers> peersHashMap = new HashMap<>(postList.size());

        for (Post post: postList) {
            Optional<Peers> peers = peersRepository.findById(post);
            if (peers.isPresent()) {
                peersHashMap.put(post, peers.get());
                continue;
            }
            log.warn("Пост с id {} Не имеет анонсированного торрента. Хеш {}", post.getPostId(), post.getHashInfo());
        }

        return peersHashMap;
    }

    @Override
    public List<Post> getAllPostsByThread(Thread thread) {
        return postRepository.findByThread(thread);
    }
    @Override
    public List<Object[]> countPostsByThread(Thread threadId) {
        return postRepository.countTotalPostsByThread(threadId);
    }

    /**
     *
     * @param torrentFile
     * Загруженный пользователем торрент файл
     * @return
     * Хеш сумма раздачи
     */
    private String registerNewTorrent(MultipartFile torrentFile) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("torrentFile", torrentFile.getResource());

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        String uri = "http://localhost:8081/register";
        URI uri1 = UriComponentsBuilder.fromUriString(uri)
                .build().toUri();
        ResponseEntity<String> response = restTemplate.postForEntity(uri1, requestEntity, String.class);

        return response.getBody();
    }
}
