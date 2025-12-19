package ru.students.forumservicediplomproject.service.forum;

import org.hamcrest.core.IsAnything;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-development.properties")
class ForumControllerMvcTest {
    @Autowired
    MockMvc mockMvc;

    @WithMockUser(username = "admin@admin.com", authorities = {"ADMIN"})
    @Test
    @Transactional
    @Rollback
    void saveForum_performRequestByAdmin_returnOk() throws Exception {
        //given
        MockHttpServletRequestBuilder request =
                post("/forum/saveForum")
                //  .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("forumName", "testForum")
                .param("description", "testDescription");

        //when
        ResultActions perform = mockMvc.perform(request);

        //verify
        perform.andExpectAll(
                status().isFound(),
                header().stringValues("location", IsAnything.anything())
        );

    }

    @WithMockUser(username = "admin@admin.com", authorities = {"ADMIN"})
    @Test
    @Transactional
    @Rollback
    void saveForum_performRequestByAdminWithEmptyName_returnOkWithFieldError() throws Exception {
        //given
        MockHttpServletRequestBuilder request =
                post("/forum/saveForum")
                //  .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("forumName", "")
                .param("description", "testDescription");

        //when
        ResultActions perform = mockMvc.perform(request);

        //verify
        perform.andExpectAll(
                status().isOk(),
                model().attributeHasFieldErrors("forum", "forumName")
        );
    }

    @WithMockUser(username = "user@user.com", authorities = {"USER"})
    @Test
    void saveForum_performRequestByUser_returnForbidden() throws Exception {
        //given
        MockHttpServletRequestBuilder request =
                post("/forum/saveForum")
                        //  .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("forumName", "testForum")
                        .param("description", "testDescription");

        //when
        ResultActions perform = mockMvc.perform(request);

        //verify
        perform.andExpectAll(
                status().isForbidden()
        );
    }

    @Test
    void saveForum_performRequestByAnonymous_returnFoundWithRedirectToLoginPage() throws Exception {
        //given
        MockHttpServletRequestBuilder request =
                post("/forum/saveForum")
                        //  .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("forumName", "testForum")
                        .param("description", "testDescription");

        //when
        ResultActions perform = mockMvc.perform(request);

        //verify
        perform.andExpectAll(
                status().isFound(),
                redirectedUrlPattern("**/login"),
                header().stringValues("location", "http://localhost/login")
        );
    }

//
//    @Test
//    void getForum() {
//        ForumDto forumDto = new ForumDto();
//        forumDto.setForumName("testForum");
//        long id = forumService.saveForum(forumDto);
//
//        Forum forum = forumService.getForum(id);
//        assertNotNull(forum);
//        System.out.println(forum.getForumName());
//        assertEquals(forum.getForumName(), "testForum");
//    }
//
//    @Test
//    void getAllForums() {
//        List<Forum> allForums = forumService.getAllForums();
//        assertEquals(0, allForums.size());
//
//        ForumDto forumDto = new ForumDto();
//        forumDto.setForumName("testForum");
//        forumService.saveForum(forumDto);
//
//        assertEquals(1, forumService.getAllForums().size());
//    }
//
//    @Test
//    void deleteForum() {
//        ForumDto forumDto = new ForumDto();
//        forumDto.setForumName("testForum");
//        long id = forumService.saveForum(forumDto);
//
//        forumService.deleteForum(id);
//        assertThrowsExactly(ResourceNotFoundException.class, () -> forumService.getForum(id));
//    }
//
//    @Test
//    void updateForum() {
//        ForumDto forumDto = new ForumDto();
//        forumDto.setForumName("testForum");
//        long id = forumService.saveForum(forumDto);
//
//        Forum forum = forumService.getForum(id);
//        assertEquals(forum.getForumName(), "testForum");
//        forum.setForumName("testForumUpdated");
//        forumService.updateForum(forum);
//        assertEquals(forumService.getForum(id).getForumName(), "testForumUpdated");
//    }
}