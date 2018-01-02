package answer.king.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import answer.king.model.Item;
import answer.king.repo.ItemRepository;

@Service
@Transactional(rollbackFor = InvalidItemException.class)
public class ItemService {

	@Autowired
	private ItemRepository itemRepository;

	@Autowired
	private ItemValidator itemValidator;

	public List<Item> getAll() {
		return itemRepository.findAll();
	}

	public Item save(Item item) throws InvalidItemException {
		itemValidator.validate(item);
		return itemRepository.save(item);
	}

	public Item updatePrice(Long itemId, BigDecimal price) throws InvalidItemException {
		Item item = itemRepository.findOne(itemId);
		item.setPrice(price);
		itemValidator.validate(item);
		return itemRepository.save(item);
	}
}
