package com.shop.repository;

import com.shop.entity.ItemImg;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemImgRepository extends JpaRepository<ItemImg, Long> {
	
	  List<ItemImg> findByItemIdOrderByIdAsc(Long itemId);
	  
      ItemImg findByItemIdAndRepimgYn(Long itemId, String repimgYn);

}