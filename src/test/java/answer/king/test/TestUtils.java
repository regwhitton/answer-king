package answer.king.test;

import static org.assertj.core.util.Lists.newArrayList;

import java.math.BigDecimal;

import answer.king.model.Item;
import answer.king.model.LineItem;
import answer.king.model.Order;
import answer.king.model.Receipt;

public class TestUtils {

	private TestUtils() {
	}

	public static Item item(Long id, String name, Double price) {
		Item item = new Item();
		item.setId(id);
		item.setName(name);
		item.setPrice(price == null ? null : BigDecimal.valueOf(price));
		return item;
	}

	public static LineItem lineItem(Long id, Item item, Integer quantity) {
		LineItem lineItem = new LineItem();
		lineItem.setId(id);
		lineItem.setName(item.getName());
		lineItem.setPrice(item.getPrice());
		lineItem.setItem(item);
		lineItem.setQuantity(quantity);
		return lineItem;
	}

	public static Order order(Long id, Boolean paid, LineItem... items) {
		Order order = new Order();
		order.setId(id);
		order.setPaid(paid);
		order.setLineItems(newArrayList(items));
		for (LineItem item : items) {
			item.setOrder(order);
		}
		return order;
	}

	public static Receipt receipt(Long id, Order order, BigDecimal payment) {
		Receipt receipt = new Receipt();
		receipt.setId(id);
		receipt.setOrder(order);
		receipt.setPayment(payment);
		return receipt;
	}
}
