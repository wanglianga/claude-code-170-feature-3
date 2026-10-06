package com.community.eldercare.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 助餐签到记录。长期照护方案安排助餐后，老人每次到助餐点/用餐配送签到，
 * 用于观察方案生效后助餐服务是否真正落实。
 */
@Getter
@Setter
@Entity
@Table(name = "meal_checkin", indexes = {
        @Index(name = "idx_meal_elder", columnList = "elder_id"),
        @Index(name = "idx_meal_plan", columnList = "plan_id"),
        @Index(name = "idx_meal_date", columnList = "meal_date")
})
public class MealCheckin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Elder elder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private LongTermCarePlan plan;

    /** 用餐日期 */
    @Column(nullable = false)
    private LocalDate mealDate = LocalDate.now();

    /** 餐次：午餐 / 晚餐 */
    @Column(nullable = false)
    private String mealType = "午餐";

    /** DINE_IN 堂食 / DELIVERY 配送上门 */
    private String deliveryMode = "DINE_IN";

    /** 是否签到（核销） */
    private boolean checkedIn;
    private LocalDateTime checkedAt;

    /** 登记人 */
    private String operatorName;

    @Column(length = 1000)
    private String note;

    private LocalDateTime createdAt = LocalDateTime.now();
}
