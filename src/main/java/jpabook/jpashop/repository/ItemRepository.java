package jpabook.jpashop.repository;

import jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepository {

    private final EntityManager em;

    public void save(Item item) {
        // item 은 jpa에 저장될 때 까지 id 값이 없다
        // id 값이 null 이라는 것은 새로 들어온 객체라는 것이다
        if(item.getId() == null) {
            em.persist(item);
        } else {
            // 아니면 이미 디비에 등록이 되어있다는 의미이다
            // 그 뜻은 업데이트를 한다는 의미이다
            // 또한 준영속성 상태의 아이템은 영속성으로 전환해서 업데이트를 해줌으로써 업데이트가 제대로 이루어질 수 있도록 돕는다
            // 주의할 점 merge는 모든 필드를 업데이트 시키기 때문에 값이 없는 필드는 null로 업데이트 해서 문제가 될 가능성이 높다
            // -> itemService의 update, 변경감지를 이용해야 함
            em.merge(item);
        }
    }

    public Item findOne(Long id) {
        return em.find(Item.class, id);
    }

    public List<Item> findAll() {
        return em.createQuery("select i from Item i", Item.class)
                .getResultList();
    }

}
