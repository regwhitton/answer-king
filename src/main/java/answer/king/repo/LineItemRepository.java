package answer.king.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import answer.king.model.LineItem;

public interface LineItemRepository extends JpaRepository<LineItem, Long> {
}