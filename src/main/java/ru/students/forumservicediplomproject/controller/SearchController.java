package ru.students.forumservicediplomproject.controller;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import ru.students.forumservicediplomproject.Search;
import ru.students.forumservicediplomproject.entity.Message;
import ru.students.forumservicediplomproject.entity.Post;
import ru.students.forumservicediplomproject.entity.Thread;
import ru.students.forumservicediplomproject.service.SearchService;

import java.util.List;

@Controller
public class SearchController {
    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/search")
    public ModelAndView search(Search keyword, BindingResult result) {
        ModelAndView modelAndView = new ModelAndView("/search/search-page");

        List<Message> messageList = searchService.messageResult(keyword.getKeyword());
        List<Post> postList = searchService.postResult(keyword.getKeyword());
        List<Thread> threadList = searchService.threadResult(keyword.getKeyword());

        if (messageList.isEmpty() ||
        postList.isEmpty() ||
        threadList.isEmpty()) {
            result.rejectValue("keyword", "not found", "Ничего не найдено по запросу");
        }

        modelAndView.addObject("search", new Search());
        modelAndView.addObject("messageResults", messageList);
        modelAndView.addObject("postResults", postList);
        modelAndView.addObject("threadResults", threadList);

        return modelAndView;
    }

}
