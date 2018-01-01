package answer.king.test;

import static org.assertj.core.util.Lists.newArrayList;

import java.math.BigDecimal;

import answer.king.model.Item;
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

	public static Order order(Long id, Boolean paid, Item... items) {
		Order order = new Order();
		order.setId(id);
		order.setPaid(paid);
		order.setItems(newArrayList(items));
		for (Item item : items) {
			item.setOrder(order);
		}
		return order;
	}

	public static Receipt receipt(Order order, BigDecimal payment) {
		Receipt receipt = new Receipt();
		receipt.setOrder(order);
		receipt.setPayment(payment);
		return receipt;
	}
}
