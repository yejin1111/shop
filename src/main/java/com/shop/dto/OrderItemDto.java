package com.shop.dto;


import com.shop.entity.OrderItem;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class OrderItemDto {

    public OrderItemDto(OrderItem orderItem, String imgUrl){ // orderItem객체와 이미지 경로를 파라미터로 받아 멤버변수 값을 세팅
        this.itemNm = orderItem.getItem().getItemNm();
        this.count = orderItem.getCount();
        this.orderPrice = orderItem.getOrderPrice();
        this.imgUrl = imgUrl;
    }

    private String itemNm; //상품명
    private int count; //주문 수량
    private int orderPrice; //주문 금액
    private String imgUrl; //상품 이미지 경로
}
