package com.shop.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.thymeleaf.util.StringUtils;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shop.constant.ItemSellStatus;
import com.shop.dto.ItemSearchDto;
import com.shop.dto.MainItemDto;
import com.shop.dto.QMainItemDto;
import com.shop.entity.Item;
import com.shop.entity.QItem;
import com.shop.entity.QItemImg;

import jakarta.persistence.EntityManager;

public class ItemRepositoryCustomImpl implements ItemRepositoryCustom { // 인터페이스를 상속하여 구현
	private JPAQueryFactory queryFactory; // 동적으로 쿼리를 생성하기 위해서 JPAQueryFactory 클래스를 사용.

	public ItemRepositoryCustomImpl(EntityManager em) {
		this.queryFactory = new JPAQueryFactory(em); // JPAQueryFactory 생성자로 EntityManager 객체를 넣어준다.
	}

	private BooleanExpression searchSellStatusEq(ItemSellStatus searchSellStatus) {
		return searchSellStatus == null ? null : QItem.item.itemSellStatus.eq(searchSellStatus);
	}
	// 상품 판매 상태조건이 전체(null)일 경우는 null을 리턴,
	// 결과값이 null이면 where절에서 해당조건은 무시. 상품판매 상태조건이 null이 아니라
	// 판매중 혹은 품절 상태라면 해당조건 상품만 조회한다.

	private BooleanExpression regDtsAfter(String searchDateType) {
		// searchDateType의 값에 따라서 dateTime의 값을 이전 시간의 값으로
		// 세팅 후 해당 시간 이후로 등록된 상품만 조회한다. 예를 들어, searchDateType의 값이 1m인 경우 dateTime의 시간을 한
		// 달 전으로 // 세팅 후 최근 한 달 동안 등록된 상품만 조회하도록 조건값을 반환한다.
		LocalDateTime dateTime = LocalDateTime.now();
		if (StringUtils.equals("all", searchDateType) || searchDateType == null) {
			return null;
		} else if (StringUtils.equals("1d", searchDateType)) {
			dateTime = dateTime.minusDays(1);
		} else if (StringUtils.equals("1w", searchDateType)) {
			dateTime = dateTime.minusWeeks(1);
		} else if (StringUtils.equals("1m", searchDateType)) {
			dateTime = dateTime.minusMonths(1);
		} else if (StringUtils.equals("6m", searchDateType)) {
			dateTime = dateTime.minusMonths(6);
		}
		return QItem.item.regTime.after(dateTime);
	}

	private BooleanExpression searchByLike(String searchBy, String searchQuery) { // searchBy의 값에 따라서 상품명에 검색어를 포함하고
		// 있는 상품 또는 상품 생성자의 아이디에 검색어를 포함하고 있는 상품을 조회하도록 조건값을 반환한다.
		if (StringUtils.equals("itemNm", searchBy)) {
			return QItem.item.itemNm.like("%" + searchQuery + "%");
		} else if (StringUtils.equals("createdBy", searchBy)) {
			return QItem.item.createdBy.like("%" + searchQuery + "%");
		}
		return null;
	}

	@Override
	public Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
		List<Item> content = queryFactory // queryFactory를 이용해서 쿼리를 생성한다.
				.selectFrom(QItem.item) // 상품 데이터를 조회하기 위해서 QItem의 item을 지정한다.
				.where(regDtsAfter(itemSearchDto.getSearchDateType()), // BooleanExpression 반환하는 조건문들을 넣어준다.
						searchSellStatusEq(itemSearchDto.getSearchSellStatus()), // 컴마(,)단위로 넣어줄 경우 and조건으로 인식한다.
						searchByLike(itemSearchDto.getSearchBy(), itemSearchDto.getSearchQuery()))
				.orderBy(QItem.item.id.desc()).offset(pageable.getOffset()) // 데이터를 가지고 올 시작 인덱스를 지정한다.
				.limit(pageable.getPageSize()) // 한번에 가지고 올 최대 개수를 지정한다.

				.fetch(); // 조회한 리스트 및 전체 개수를 포함하는 QueryResult를 반환한다. 상품 데이터 리스트 조회 및 상품 데이터 전체 개수를 //
							// 조회하는 2번의 쿼리문이 실행된다.
		long total = queryFactory.select(Wildcard.count).from(QItem.item)
				.where(regDtsAfter(itemSearchDto.getSearchDateType()),
						searchSellStatusEq(itemSearchDto.getSearchSellStatus()),
						searchByLike(itemSearchDto.getSearchBy(), itemSearchDto.getSearchQuery()))
				.fetchOne();
		return new PageImpl<>(content, pageable, total); // 조회한 데이터를 Page 클래스의 구현체인 PageImpl 객체로 반환한다.

	}

	private BooleanExpression itemNmLike(String searchQuery) {
		return StringUtils.isEmpty(searchQuery) ? null : QItem.item.itemNm.like("%" + searchQuery + "%");
	}

	@Override
	public Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
		QItem item = QItem.item;
		QItemImg itemImg = QItemImg.itemImg;

		List<MainItemDto> content = queryFactory
				.select(new QMainItemDto(item.id, item.itemNm, item.itemDetail, itemImg.imgUrl, item.price)

				).from(itemImg).join(itemImg.item, item).where(itemImg.repimgYn.eq("Y")) // 상품 이미지의 경우 대표 상품 이미지만 불러온다

				.where(itemNmLike(itemSearchDto.getSearchQuery()))

				.orderBy(item.id.desc()).offset(pageable.getOffset()).limit(pageable.getPageSize()).fetch();

		long total = queryFactory.select(Wildcard.count).from(itemImg).join(itemImg.item, item)
				.where(itemImg.repimgYn.eq("Y")).where(itemNmLike(itemSearchDto.getSearchQuery())).fetchOne();
		return new PageImpl<>(content, pageable, total);
	}

}
