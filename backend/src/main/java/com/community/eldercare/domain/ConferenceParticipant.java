package com.community.eldercare.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 个案会商参与方：养老顾问、社区医生、家属、社工等。
 */
@Getter
@Setter
@Entity
@Table(name = "case_conference_participant")
public class ConferenceParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private CaseConference conference;

    /** SOCIAL_WORKER 社工 / ELDER_ADVISOR 养老顾问 / COMMUNITY_DOCTOR 社区医生 /
     *  FAMILY 家属 / GRID_WORKER 网格员 / OTHER 其他 */
    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private String name;
    private String phone;

    /** 是否实际参加（家属可能缺席） */
    private boolean attended = true;

    /** 个人意见 */
    @Column(length = 1000)
    private String opinion;
}
