package ru.students.forumservicediplomproject.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class PeersDto {
    private int leechers;
    private int seeders;
    private String hash;
}
