package answer.king.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import answer.king.model.Item;
import answer.king.model.LineItem;
import answer.king.model.Order;
import answer.king.model.Receipt;
import answer.king.repo.ItemRepository;
import answer.king.repo.LineItemRepository;
import answer.king.repo.OrderRepository;
import answer.king.repo.ReceiptRepository;

@Service
@Transactional(rollbackFor = InsufficientPaymentException.class)
public class OrderService {

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private ItemRepository itemRepository;

	@Autowired
	private ReceiptRepository receiptRepository;

	@Autowired
	private LineItemRepository lineItemRepository;

	public List<Order> getAll() {
		return orderRepository.findAll();
	}

	public Order save(Order order) {
		return orderRepository.save(order);
	}

	public void addItem(Long id, Long itemId) {
		Order order = orderRepository.findOne(id);
		Item item = itemRepository.findOne(itemId);

		LineItem lineItem = generateLineItem(item, order);

		order.getLineItems().add(lineItem);
		orderRepository.save(order);
	}

	private LineItem generateLineItem(Item item, Order order) {
		LineItem lineItem = new LineItem();
		lineItem.setName(item.getName());
		lineItem.setPrice(item.getPrice());
		lineItem.setOrder(order);
		lineItem.setItem(item);
		lineItem.setQuantity(1);
		return lineItemRepository.save(lineItem);
	}

	public Receipt pay(Long id, BigDecimal payment) throws InsufficientPaymentException {
		Order order = orderRepository.findOne(id);
		validatePaymentIsSufficientForOrder(payment, order);
		updateOrderAsPaid(order);
		return generateReceiptForPaymentOfOrder(payment, order);
	}

	private void validatePaymentIsSufficientForOrder(BigDecimal payment, Order order)
			throws InsufficientPaymentException {
		if (payment.compareTo(totalOrderPrice(order)) < 0) {
			throw new InsufficientPaymentException("insufficient payment");
		}
	}

	private BigDecimal totalOrderPrice(Order order) {
		return order.getLineItems().stream().map(LineItem::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private void updateOrderAsPaid(Order order) {
		order.setPaid(true);
		orderRepository.save(order);
	}

	private Receipt generateReceiptForPaymentOfOrder(BigDecimal payment, Order order) {
		Receipt receipt = receiptForPaymentOfOrder(payment, order);
		return receiptRepository.save(receipt);
	}

	private Receipt receiptForPaymentOfOrder(BigDecimal payment, Order order) {
		Receipt receipt = new Receipt();
		receipt.setPayment(payment);
		receipt.setOrder(order);
		return receipt;
	}
}