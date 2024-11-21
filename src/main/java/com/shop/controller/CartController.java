package com.shop.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.shop.dto.CartDetailDto;
import com.shop.dto.CartItemDto;
import com.shop.dto.CartOrderDto;
import com.shop.service.CartService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping(value = "/cart")
    public @ResponseBody ResponseEntity order(@RequestBody @Valid CartItemDto cartItemDto, BindingResult bindingResult, Principal principal){

       if(bindingResult.hasErrors()){
            StringBuilder sb = new StringBuilder();
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();

            for (FieldError fieldError : fieldErrors) {
                sb.append(fieldError.getDefaultMessage());
            }

            return new ResponseEntity<String>(sb.toString(), HttpStatus.BAD_REQUEST);
        }

        String email = principal.getName();
        Long cartItemId;

        try {
            cartItemId = cartService.addCart(cartItemDto, email);
        } catch(Exception e){
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<Long>(cartItemId, HttpStatus.OK);
    }
    

    @GetMapping(value = "/cart")
	public String orderHist(Principal principal, Model model) {
		List<CartDetailDto> cartDetailList = cartService.getCartList(principal.getName());
// 현재 로그인한 사용자의 이메일 정보를 이용하여 장바구니에 담겨 있는 상품 정보를 조회한다.
		model.addAttribute("cartItems", cartDetailList);
// 조회한 장바구니 상품 정보를 뷰로 전달한다.
		return "cart/cartList";
	}


    @PatchMapping(value = "/cartItem/{cartItemId}")  // http 메소드에서 요청된 자원 일부를 업데이트할 땐 @PatchMapping을 사용한다.
    public @ResponseBody ResponseEntity updateCartItem(@PathVariable("cartItemId") Long cartItemId, int count, Principal principal){

        if(count <= 0){  // 장바구니에 담겨있는 상품의 개수를 0개 이하로 업데이트 요창할 때 에러 메시지를 담아서 반환한다.
            return new ResponseEntity<String>("최소 1개 이상 담아주세요", HttpStatus.BAD_REQUEST);
        } else if(!cartService.validateCartItem(cartItemId, principal.getName())){  // 수정 권한을 체크한다.
            return new ResponseEntity<String>("수정 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        cartService.updateCartItemCount(cartItemId, count);  // 장바구니 상품의 개수를 업데이트한다.
        return new ResponseEntity<Long>(cartItemId, HttpStatus.OK);
    }
   
    @DeleteMapping(value = "/cartItem/{cartItemId}")  // http메소드에서 DELETE 경우 자원을 삭제하는 어노테이션
    public @ResponseBody ResponseEntity deleteCartItem(@PathVariable("cartItemId") Long cartItemId, Principal principal){

        if(!cartService.validateCartItem(cartItemId, principal.getName())){  // 수정 권한 체크
            return new ResponseEntity<String>("수정 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        cartService.deleteCartItem(cartItemId);   // 해당 장바구니 상품을 삭제한다.

        return new ResponseEntity<Long>(cartItemId, HttpStatus.OK);
    }
    
    @PostMapping(value = "/cart/orders")
    public @ResponseBody ResponseEntity orderCartItem(@RequestBody CartOrderDto cartOrderDto, Principal principal){

        List<CartOrderDto> cartOrderDtoList = cartOrderDto.getCartOrderDtoList();

        if(cartOrderDtoList == null || cartOrderDtoList.size() == 0){  // 주문할 상품을 선택하지 않았는지 체크한다.
            return new ResponseEntity<String>("주문할 상품을 선택해주세요", HttpStatus.FORBIDDEN);
        }

        for (CartOrderDto cartOrder : cartOrderDtoList) {  // 주문 권한을 체크한다.
            if(!cartService.validateCartItem(cartOrder.getCartItemId(), principal.getName())){  // 주문 로직 호출 결과 생성된 주문번호를 반환 받는다.
                return new ResponseEntity<String>("주문 권한이 없습니다.", HttpStatus.FORBIDDEN);
            }
        }

        Long orderId = cartService.orderCartItem(cartOrderDtoList, principal.getName());
        return new ResponseEntity<Long>(orderId, HttpStatus.OK);  // 생성된 주문 번호와 요청이 성공했다는 http 응답 상태 코드를 반환한다.
    }
}	
