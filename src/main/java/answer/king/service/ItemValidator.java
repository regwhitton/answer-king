package answer.king.service;

import static org.springframework.util.StringUtils.isEmpty;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import answer.king.model.Item;

@Component
public class ItemValidator {

	public void validate(Item item) throws InvalidItemException {
		if (item == null) {
			throw new InvalidItemException("item must be provided");
		}
		validateName(item.getName());
		validatePrice(item.getPrice());
	}

	private void validateName(String name) throws InvalidItemException {
		if (isEmpty(name)) {
			throw new InvalidItemException("item name must be provided");
		}
	}

	private void validatePrice(BigDecimal price) throws InvalidItemException {
		if (price == null) {
			throw new InvalidItemException("item price must be provided");
		}
		if (price.compareTo(BigDecimal.ZERO) < 0) {
			throw new InvalidItemException("item price cannot be negative");
		}
	}
}
