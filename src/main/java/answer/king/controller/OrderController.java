package answer.king.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import answer.king.model.Order;
import answer.king.model.Receipt;
import answer.king.service.InsufficientPaymentException;
import answer.king.service.OrderService;

@RestController
@RequestMapping("/order")
public class OrderController {

	@Autowired
	private OrderService orderService;

	@RequestMapping(method = RequestMethod.GET)
	public List<Order> getAll() {
		return orderService.getAll();
	}

	@RequestMapping(method = RequestMethod.POST)
	public Order create() {
		return orderService.save(new Order());
	}

	@RequestMapping(value = "/{id}/addItem/{itemId}", method = RequestMethod.PUT)
	public void addItem(@PathVariable("id") Long id, @PathVariable("itemId") Long itemId) {
		orderService.addItem(id, itemId);
	}

	@RequestMapping(value = "/{id}/pay", method = RequestMethod.PUT)
	public Receipt pay(@PathVariable("id") Long id, @RequestBody BigDecimal payment)
			throws InsufficientPaymentException {
		return orderService.pay(id, payment);
	}

	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	@ExceptionHandler(InsufficientPaymentException.class)
	@ResponseBody
	public Error handleException(InsufficientPaymentException e) {
		return new Error(e.getMessage());
	}
}
