package answer.king.service;

import static answer.king.test.TestUtils.item;
import static answer.king.test.TestUtils.order;
import static answer.king.test.TestUtils.receipt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Matchers.*;
import static org.mockito.Matchers.refEq;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import answer.king.model.Order;
import answer.king.model.Receipt;
import answer.king.repo.ItemRepository;
import answer.king.repo.OrderRepository;
import answer.king.repo.ReceiptRepository;

@RunWith(MockitoJUnitRunner.class)
public class OrderServiceTest {

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private ItemRepository itemRepository;

	@Mock
	private ReceiptRepository receiptRepository;

	@InjectMocks
	private OrderService orderService;

	@Captor
	private ArgumentCaptor<Order> orderCaptor;

	@Captor
	private ArgumentCaptor<Receipt> receiptCaptor;

	@Test
	public void getAllShouldFindAllOrdersInRepository() {
		// Given
		Long orderId = 101L;
		given(orderRepository.findAll()).willReturn(newArrayList(order(orderId, false)));

		// when
		List<Order> orders = orderService.getAll();

		// then
		assertThat(orders).usingFieldByFieldElementComparator().containsExactly(order(orderId, false));
	}

	@Test
	public void saveShouldSaveOrderToRepositoryAndReturnOrderUpdatedWithId() throws Exception {
		// Given
		Order inputOrder = order(null, false);
		Order orderUpdatedWithId = order(3L, false);

		given(orderRepository.save(refEq(inputOrder))).willReturn(orderUpdatedWithId);

		// when
		Order returnedOrder = orderService.save(inputOrder);

		// then
		assertThat(returnedOrder).isEqualToComparingFieldByField(orderUpdatedWithId);
	}

	@Test
	public void addItemShouldAttachGivenItemToGivenOrderInRepository() {
		// Given
		Long orderId = 101L;
		Long itemId = 202L;

		given(orderRepository.findOne(eq(orderId))).willReturn(order(orderId, false));
		given(itemRepository.findOne(eq(itemId))).willReturn(item(itemId, "itemName", 10.0));

		// when
		orderService.addItem(orderId, itemId);

		// then
		then(orderRepository).should().save(orderCaptor.capture());

		Order savedOrder = orderCaptor.getValue();
		Order expectedOrder = order(orderId, false, item(itemId, "itemName", 10.0));

		assertThat(savedOrder).isEqualToComparingFieldByFieldRecursively(expectedOrder);
		assertThat(savedOrder.getItems().get(0).getOrder()).isEqualTo(savedOrder);
	}

	@Test
	public void payShouldMarkOrderAsPaidInRepository() throws Exception {
		// Given
		Long orderId = 101L;
		Long itemId = 202L;
		Order existingOrder = order(orderId, false, item(itemId, "itemName", 9.99));
		BigDecimal payment = BigDecimal.TEN;
		given(orderRepository.findOne(eq(orderId))).willReturn(existingOrder);

		Long receiptId = 303L;
		Receipt persistedReceipt = receipt(order(orderId, true, item(itemId, "itemName", 9.99)), payment);
		persistedReceipt.setId(receiptId);
		given(receiptRepository.save(any(Receipt.class))).willReturn(persistedReceipt);

		// when
		Receipt returnedReceipt = orderService.pay(orderId, payment);

		// then
		then(orderRepository).should().save(orderCaptor.capture());
		Order savedOrder = orderCaptor.getValue();
		Order expectedOrder = order(orderId, true, item(itemId, "itemName", 9.99));
		assertThat(savedOrder).isEqualToComparingFieldByFieldRecursively(expectedOrder);

		then(receiptRepository).should().save(receiptCaptor.capture());
		Receipt savedReceipt = receiptCaptor.getValue();
		Receipt expectedReceipt = receipt(expectedOrder, payment);
		assertThat(savedReceipt).isEqualToComparingFieldByFieldRecursively(expectedReceipt);

		assertThat(returnedReceipt).isEqualToComparingFieldByFieldRecursively(persistedReceipt);
	}

	@Test(expected = InsufficientPaymentException.class)
	public void payShouldFailIfPaymentIsInsufficient() throws Exception {
		// Given
		Long orderId = 101L;
		Order order = order(orderId, false, item(202L, "itemName", 10.01));
		BigDecimal payment = BigDecimal.TEN;

		given(orderRepository.findOne(eq(orderId))).willReturn(order);

		// when
		orderService.pay(orderId, payment);

		// then exception
	}
}