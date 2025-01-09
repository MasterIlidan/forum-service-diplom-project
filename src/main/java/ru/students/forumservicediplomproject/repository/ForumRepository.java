package ru.students.forumservicediplomproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.students.forumservicediplomproject.entity.Forum;

import java.util.List;

@Repository
public interface ForumRepository extends JpaRepository<Forum, Long> {

/*    @Query("SELECT t.threadName, count(Thread.threadName) as totalThreads" +
            " from Thread as t group by t.threadName")
    List<IThreadCount> countTotalThreadsByThreadNameInterface();*/

    //SELECT t.FORUM_ID_FORUM_ID, COUNT(FORUM_ID_FORUM_ID) as threadsCount FROM Thread AS t where t.FORUM_ID_FORUM_ID=1;

    @Query("SELECT t.forumId, COUNT(t.forumId) FROM Thread AS t where t.forumId=:#{#forumId}")
    List<Object[]> countTotalThreadsByThreadsName(@Param("forumId") Forum forum);
}
