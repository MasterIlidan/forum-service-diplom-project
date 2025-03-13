package ru.students.forumservicediplomproject.controller;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import ru.students.forumservicediplomproject.controller.form.Search;
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

        if (keyword == null) keyword = new Search();

        List<Message> messageList;
        List<Post> postList;
        List<Thread> threadList;

        if (keyword.getKeyword() == null || keyword.getKeyword().isEmpty()) {
            messageList = List.of();
            postList = List.of();
            threadList = List.of();
        } else {
            messageList = searchService.messageResult(keyword.getKeyword());
            postList = searchService.postResult(keyword.getKeyword());
            threadList = searchService.threadResult(keyword.getKeyword());
        }

        modelAndView.addObject("search", keyword);
        modelAndView.addObject("messageResults", messageList);
        modelAndView.addObject("postResults", postList);
        modelAndView.addObject("threadResults", threadList);

        return modelAndView;
    }

}
