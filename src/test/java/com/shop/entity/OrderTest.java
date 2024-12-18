package com.shop.entity;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import com.shop.constant.ItemSellStatus;
import com.shop.repository.ItemRepository;
import com.shop.repository.MemberRepository;
import com.shop.repository.OrderItemRepository;
import com.shop.repository.OrderRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.properties")
@Transactional
class OrderTest {

	@Autowired
	OrderRepository orderRepository;

	@Autowired
	ItemRepository itemRepository;

	@PersistenceContext
	EntityManager em;

	@Autowired
	MemberRepository memberRepository;

	@Autowired
	OrderItemRepository orderItemRepository;

	public Item createItem() {
		Item item = new Item();
		item.setItemNm("테스트 상품");
		item.setPrice(10000);
		item.setItemDetail("상세설명");
		item.setItemSellStatus(ItemSellStatus.SELL);
		item.setStockNumber(100);
		item.setRegTime(LocalDateTime.now());
		item.setUpdateTime(LocalDateTime.now());
		return item;
	}

//    @Test
//    @DisplayName("영속성 전이 테스트")
//    public void cascadeTest() {
//
//        Order order = new Order();
//
//        for(int i=0;i<3;i++){
//            Item item = this.createItem();
//            itemRepository.save(item);
//            OrderItem orderItem = new OrderItem();
//            orderItem.setItem(item);
//            orderItem.setCount(10);
//            orderItem.setOrderPrice(1000);
//            orderItem.setOrder(order);
//            order.getOrderItems().add(orderItem); // 아직 영속성 컨텍스트에 저장되지 않은 OrderItem엔티티를 Order엔티티에 저장
//        }
//
//        orderRepository.saveAndFlush(order); // Order엔티티를 저장하면서 강제로 flush를 호출하여 영속성 컨텍스트에 있는  객체들을 DB에
//        em.clear();     // 영속성 상태를  초기화한다.                                                                                                                 반영한다.
// 
//        Order savedOrder = orderRepository.findById(order.getId()) // DB에서 주문 엔티티를 조회한다.
//                .orElseThrow(EntityNotFoundException::new);
//        assertEquals(3, savedOrder.getOrderItems().size());   // 주문상품 엔티티 3개가 실제로 DB에 저장되었는지 검사한다.
//    }

	public Order createOrder() { // 주문 데이터를 생성해서 저장하는 메서드를 만든다.
		Order order = new Order();
		for (int i = 0; i < 3; i++) {
			Item item = createItem();
			itemRepository.save(item);
			OrderItem orderItem = new OrderItem();
			orderItem.setItem(item);
			orderItem.setCount(10);
			orderItem.setOrderPrice(1000);
			orderItem.setOrder(order);
			order.getOrderItems().add(orderItem);
		}
		Member member = new Member();
		memberRepository.save(member);
		order.setMember(member);
		orderRepository.save(order);
		return order;
	}

//	@Test
//	@DisplayName("고아객체 제거 테스트")
//	public void orphanRemovalTest() {
//		Order order = this.createOrder();
//		order.getOrderItems().remove(0); // Order 엔티티에서 관리하고 있는 OrderItem 리스트의 0번째 인덱스 요소를 제거한다.
//		em.flush(); // 부모 객체와 자식 객체의 연관 관계를 끊으면 delete(삭제)가 이뤄진다.
//	}

	@Test
	@DisplayName("지연 로딩 테스트")
	public void lazyLoadingTest() {
		Order order = this.createOrder();
		Long orderItemId = order.getOrderItems().get(0).getId(); // 기존 만들었던 주문 생성 메소드를 이용하여 주문 데이
		em.flush();
		em.clear();
		OrderItem orderItem = orderItemRepository.findById(orderItemId).orElseThrow(EntityNotFoundException::new);
		System.out.println("Order class : " + orderItem.getOrder().getClass());
		System.out.println("===========================");
		orderItem.getOrder().getOrderDate();
		System.out.println("===========================");
	}
}