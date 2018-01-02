package answer.king.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import answer.king.model.Receipt;

public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
}