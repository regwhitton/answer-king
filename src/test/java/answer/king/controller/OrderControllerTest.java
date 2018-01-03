package answer.king.controller;

import static answer.king.test.TestUtils.item;
import static answer.king.test.TestUtils.lineItem;
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
		Order order = order(1L, false, lineItem(3L, item(2L, "itemName", 10.0), 1));
		given(orderService.getAll()).willReturn(newArrayList(order));

		// when & then
		mvc.perform(get("/order").accept(APPLICATION_JSON)) //
				.andExpect(status().isOk()) //
				.andExpect(content().json(
						"[{'id':1, 'paid':false, 'items':[{'id':3, 'name':'itemName', 'price':10, 'quantity':1}]}]"));
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
		Integer defaultQuantity = 1;

		// when
		mvc.perform(put("/order/" + orderId + "/addItem/" + itemId)).andExpect(status().isOk());

		// then
		then(orderService).should().addItem(eq(orderId), eq(itemId), eq(defaultQuantity));
	}

	@Test
	public void putWithQuantityShouldAddItemWithQuantity() throws Exception {
		// Given
		Long orderId = 101L;
		Long itemId = 202L;
		Integer quantity = 22;

		// when
		mvc.perform(put("/order/" + orderId + "/addItem/" + itemId + "/quantity/" + quantity))
				.andExpect(status().isOk());

		// then
		then(orderService).should().addItem(eq(orderId), eq(itemId), eq(quantity));
	}

	@Test
	public void putPaymentShouldPayOrderUsingOrderService() throws Exception {
		// Given
		Long orderId = 101L;
		Long itemId = 202L;
		Long lineItemId = 303L;
		Long receiptId = 404L;

		BigDecimal payment = BigDecimal.TEN;
		Double price = 25.0;
		Integer quantity = 1;
		Double expectedChange = payment.doubleValue() - price;

		Order order = order(orderId, false, lineItem(lineItemId, item(itemId, "itemName", price), quantity));
		Receipt receipt = receipt(receiptId, order, payment);
		given(orderService.pay(eq(orderId), eq(payment))).willReturn(receipt);

		// when & then
		mvc.perform(//
				put("/order/" + orderId + "/pay") //
						.contentType(APPLICATION_JSON).content(payment.toString()).accept(APPLICATION_JSON)) //
				.andExpect(status().isOk()) //
				.andExpect(content().json("{'payment':" + payment + ",'order':{'id':" + orderId
						+ ", 'paid':false, 'items':[{'id':" + lineItemId + ",'name':'itemName','price':" + price
						+ ",'quantity':" + quantity + "}]},'change':" + expectedChange + "}"));
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