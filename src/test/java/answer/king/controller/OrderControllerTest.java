package answer.king.controller;

import static answer.king.test.TestUtils.item;
import static answer.king.test.TestUtils.order;
import static answer.king.test.TestUtils.receipt;
import static org.assertj.core.util.Lists.newArrayList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.refEq;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import answer.king.model.Order;
import answer.king.model.Receipt;
import answer.king.service.InsufficientPaymentException;
import answer.king.service.OrderService;

@RunWith(SpringRunner.class)
@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc
public class OrderControllerTest {

	@MockBean
	private OrderService orderService;

	@Autowired
	private MockMvc mvc;

	@Captor
	private ArgumentCaptor<Order> orderCaptor;

	@Test
	public void getShouldGetAllOrdersFromOrderService() throws Exception {
		// Given
		given(orderService.getAll()).willReturn(newArrayList(order(1L, false, item(2L, "itemName", 10.0))));

		// when & then
		mvc.perform(get("/order").accept(APPLICATION_JSON)) //
				.andExpect(status().isOk()) //
				.andExpect(
						content().json("[{'id':1, 'paid':false, 'items':[{'id':2, 'name':'itemName', 'price':10}]}]"));
	}

	@Test
	public void postShouldCreateOrderUsingOrderService() throws Exception {
		// Given
		given(orderService.save(refEq(new Order()))).willReturn(order(3L, false));

		// when & then
		mvc.perform(post("/order").accept(APPLICATION_JSON)) //
				.andExpect(status().isOk()) //
				.andExpect(content().json("{'id':3, 'paid':false, 'items':[]}"));
	}

	@Test
	public void putItemIdShouldAddItemToOrderUsingOrderService() throws Exception {
		// Given
		Long orderId = 101L;
		Long itemId = 202L;

		// when
		mvc.perform(put("/order/" + orderId + "/addItem/" + itemId)).andExpect(status().isOk());

		// then
		then(orderService).should().addItem(eq(orderId), eq(itemId));
	}

	@Test
	public void putPaymentShouldPayOrderUsingOrderService() throws Exception {
		// Given
		Long orderId = 101L;
		Long itemId = 202L;
		BigDecimal payment = BigDecimal.TEN;
		Double price = 25.0;
		Double expectedChange = payment.doubleValue() - price;

		Receipt receipt = receipt(order(orderId, false, item(itemId, "itemName", price)), payment);
		given(orderService.pay(eq(orderId), eq(payment))).willReturn(receipt);

		// when & then
		mvc.perform(//
				put("/order/" + orderId + "/pay") //
						.contentType(APPLICATION_JSON).content(payment.toString()).accept(APPLICATION_JSON)) //
				.andExpect(status().isOk()) //
				.andExpect(content().json("{'payment':" + payment + ",'order':{'id':" + orderId
						+ ", 'paid':false, 'items':[{'id':" + itemId + ",'name':'itemName','price':" + price
						+ "}]},'change':" + expectedChange + "}"));
	}

	@Test
	public void putPaymentShouldReturn400WhenPaymentIsInsuffcient() throws Exception {
		// Given
		Long orderId = 101L;
		BigDecimal payment = BigDecimal.TEN;

		given(orderService.pay(eq(orderId), eq(payment)))
				.willThrow(new InsufficientPaymentException("insufficient payment"));

		// when & then
		mvc.perform(//
				put("/order/" + orderId + "/pay") //
						.contentType(APPLICATION_JSON).content(payment.toString()).accept(APPLICATION_JSON)) //
				.andExpect(status().isBadRequest()) //
				.andExpect(content().json("{'error':'insufficient payment'}"));
	}
}