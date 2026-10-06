package com.community.eldercare.dto;

import java.time.LocalDate;

/**
 * 助餐签到登记
 */
public class MealCheckinRequest {
    private LocalDate mealDate;
    /** 午餐 / 晚餐 */
    private String mealType;
    /** DINE_IN 堂食 / DELIVERY 配送上门 */
    private String deliveryMode;
    private Boolean checkedIn;
    private String note;

    public LocalDate getMealDate() { return mealDate; }
    public void setMealDate(LocalDate mealDate) { this.mealDate = mealDate; }
    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }
    public String getDeliveryMode() { return deliveryMode; }
    public void setDeliveryMode(String deliveryMode) { this.deliveryMode = deliveryMode; }
    public Boolean getCheckedIn() { return checkedIn; }
    public void setCheckedIn(Boolean checkedIn) { this.checkedIn = checkedIn; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
