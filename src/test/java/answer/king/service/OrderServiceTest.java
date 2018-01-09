package answer.king.service;

import static answer.king.test.TestUtils.item;
import static answer.king.test.TestUtils.lineItem;
import static answer.king.test.TestUtils.order;
import static answer.king.test.TestUtils.receipt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.never;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import answer.king.model.Item;
import answer.king.model.LineItem;
import answer.king.model.Order;
import answer.king.model.Receipt;
import answer.king.repo.ItemRepository;
import answer.king.repo.LineItemRepository;
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

	@Mock
	private LineItemRepository lineItemRepository;

	@InjectMocks
	private OrderService orderService;

	@Captor
	private ArgumentCaptor<Order> orderCaptor;

	@Captor
	private ArgumentCaptor<Receipt> receiptCaptor;

	@Captor
	private ArgumentCaptor<LineItem> lineItemCaptor;

	@Test
	public void getAllShouldFindAllOrdersInRepository() {
		// Given
		long orderId = 1010L;
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
	public void addItemShouldCreateLineItemAndAttachToOrderInRepository() {
		// Given
		long orderId = 1010L;
		long itemId = 2020L;
		long lineItemId = 3030L;
		int quantity = 22;

		Order order = order(orderId, false);
		Item item = item(itemId, "itemName", 10.0);
		LineItem persistedLineItem = lineItem(lineItemId, item, quantity);

		given(orderRepository.findOne(eq(orderId))).willReturn(order);
		given(itemRepository.findOne(eq(itemId))).willReturn(item);
		given(lineItemRepository.save(any(LineItem.class))).willReturn(persistedLineItem);

		// when
		orderService.addItem(orderId, itemId, quantity);

		// then
		then(lineItemRepository).should().save(lineItemCaptor.capture());
		LineItem savedLineItem = lineItemCaptor.getValue();
		assertThat(savedLineItem.getId()).isNull();
		assertThat(savedLineItem.getName()).isEqualTo("itemName");
		assertThat(savedLineItem.getPrice()).isEqualByComparingTo(BigDecimal.TEN);
		assertThat(savedLineItem.getQuantity()).isEqualTo(quantity);
		assertThat(savedLineItem.getItem()).isSameAs(item);
		assertThat(savedLineItem.getOrder()).isSameAs(order);

		then(orderRepository).should().save(orderCaptor.capture());
		Order savedOrder = orderCaptor.getValue();
		assertThat(savedOrder).isSameAs(order);
		assertThat(savedOrder.getLineItems()).hasSize(1);
		assertThat(savedOrder.getLineItems().get(0)).isSameAs(persistedLineItem);
	}

	@Test
	public void addItemShouldAddQuantityToExistingLineItem() {
		// Given
		long orderId = 1010L;
		long itemId = 2020L;
		long lineItemId = 3030L;
		int existingQuantity = 11;
		int newQuantity = 22;

		Item item = item(itemId, "itemName", 10.0);
		LineItem lineItem = lineItem(lineItemId, item, existingQuantity);
		Order order = order(orderId, false, lineItem);

		given(orderRepository.findOne(eq(orderId))).willReturn(order);
		given(itemRepository.findOne(eq(itemId))).willReturn(item);

		// when
		orderService.addItem(orderId, itemId, newQuantity);

		// then
		then(lineItemRepository).should().save(lineItemCaptor.capture());
		LineItem savedLineItem = lineItemCaptor.getValue();
		assertThat(savedLineItem).isSameAs(lineItem);
		assertThat(savedLineItem.getQuantity()).isEqualTo(existingQuantity + newQuantity);
		assertThat(savedLineItem.getId()).isEqualTo(lineItemId);
		assertThat(savedLineItem.getName()).isEqualTo("itemName");
		assertThat(savedLineItem.getPrice()).isEqualByComparingTo(BigDecimal.TEN);
		assertThat(savedLineItem.getItem()).isSameAs(item);
		assertThat(savedLineItem.getOrder()).isSameAs(order);

		then(orderRepository).should(never()).save(any(Order.class));
	}

	@Test
	public void payShouldMarkOrderAsPaidInRepository() throws Exception {
		// Given
		long orderId = 1010L;
		long itemId = 2020L;
		long receiptId = 3030L;
		long lineItemId = 3030L;
		BigDecimal payment = BigDecimal.TEN;

		LineItem existingLineItem = lineItem(lineItemId, item(itemId, "itemName", 9.99), 1);
		Order existingOrder = order(orderId, false, existingLineItem);
		given(orderRepository.findOne(eq(orderId))).willReturn(existingOrder);

		Order paidOrder = order(orderId, true, existingLineItem);
		Receipt receiptWithId = receipt(receiptId, paidOrder, payment);
		given(receiptRepository.save(any(Receipt.class))).willReturn(receiptWithId);

		// when
		Receipt returnedReceipt = orderService.pay(orderId, payment);

		// then
		then(orderRepository).should().save(orderCaptor.capture());
		Order savedOrder = orderCaptor.getValue();
		Order expectedPaidOrder = order(orderId, true, existingLineItem);
		assertThat(savedOrder).isEqualToComparingFieldByFieldRecursively(expectedPaidOrder);

		then(receiptRepository).should().save(receiptCaptor.capture());
		Receipt savedReceipt = receiptCaptor.getValue();
		Receipt expectedReceiptWithoutId = receipt(null, expectedPaidOrder, payment);
		assertThat(savedReceipt).isEqualToComparingFieldByFieldRecursively(expectedReceiptWithoutId);

		Receipt expectedReceiptWithId = receipt(receiptId, expectedPaidOrder, payment);
		assertThat(returnedReceipt).isEqualToComparingFieldByFieldRecursively(expectedReceiptWithId);
	}

	@Test(expected = InsufficientPaymentException.class)
	public void payShouldFailIfPaymentIsInsufficient() throws Exception {
		// Given
		long orderId = 1010L;
		Order order = order(orderId, false, lineItem(303L, item(202L, "itemName", 10.01), 1));
		BigDecimal payment = BigDecimal.TEN;

		given(orderRepository.findOne(eq(orderId))).willReturn(order);

		// when
		orderService.pay(orderId, payment);

		// then exception
	}
}