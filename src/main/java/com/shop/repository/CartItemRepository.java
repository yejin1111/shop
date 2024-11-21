package com.shop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.shop.dto.CartDetailDto;
import com.shop.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
	CartItem findByCartIdAndItemId(Long cartId, Long itemId);

//생성자의 파라미터 순서는 DTO클래스에 명시한 순서로 넣어주어야 한다.
//장바구니에 담겨있는 상품의 대표 이미지만 가지고 오도록 조건문을 작성
	@Query("select new com.shop.dto.CartDetailDto(ci.id, i.itemNm, i.price, ci.count, im.imgUrl) "
			+ "from CartItem ci, ItemImg im " + "join ci.item i " + "where ci.cart.id = :cartId "
			+ "and im.item.id = ci.item.id " + "and im.repimgYn = 'Y' " + "order by ci.regTime desc")
	List<CartDetailDto> findCartDetailDtoList(Long cartId);
}