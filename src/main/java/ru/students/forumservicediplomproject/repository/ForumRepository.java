package ru.students.forumservicediplomproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.students.forumservicediplomproject.entity.Forum;

import java.util.List;

@Repository
public interface ForumRepository extends JpaRepository<Forum, Long> {

/*    @Query("SELECT t.threadName, count(Thread.threadName) as totalThreads" +
            " from Thread as t group by t.threadName")
    List<IThreadCount> countTotalThreadsByThreadNameInterface();*/
    @Query("SELECT t.threadName, COUNT(t.threadName) FROM Thread AS t GROUP BY t.threadName")
    List<Object[]> countTotalThreadsByThreadsName();
}
