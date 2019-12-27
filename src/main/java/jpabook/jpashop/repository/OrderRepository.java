package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

    // 주문 검색
    public List<Order> findAllByString(OrderSearch orderSearch) {

        /*
        return em.createQuery("select o from order o join o.member m" +
                " where o.status = :status " +
                " and m.name like :name", Order.class)
                .setParameter("status", orderSearch.getOrderStatus())
                .setParameter("name", orderSearch.getMemberName())
                .setMaxResults(1000) // 최대 1000건 조회
                .getResultList();
        */
        // 동적으로 쿼리를 생성하려면 어떻게 해야 할까?
        String jpql = "select o From Order o join o.member m";
        boolean isFirstCondition = true;
        // 주문 상태 검색
        if(orderSearch.getOrderStatus() != null) {
            if(isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }
        // 회원 이름 검색
        if(StringUtils.hasText(orderSearch.getMemberName())) {
            if(isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }

        TypedQuery<Order> query = em.createQuery(jpql, Order.class)
                .setMaxResults(1000);

        if(orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if(StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }

        return query.getResultList();
    }

    // 동적 쿼리 빌드 Querydsl
    // 아래 코드는 실무에서는 잘 안쓴다고 한다
    /*
    public List<Order> findAllByCriteria(OrderSearch orderSearch) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Object, Object> m = o.join("member", JoinType.INNER);

        List<Predicate> criteria = new ArrayList<>();

        // 주문 상태 검색
        if(orderSearch.getOrderStatus() != null) {
            Predicate status = cb.equal(o.get("status"), orderSearch.getOrderStatus());
            criteria.add(status);
        }
        // 회원 이름 검색
        if(StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name = cb.like(m.<String>get("name"), "%" + orderSearch.getMemberName() + "%");
            criteria.add(name);
        }

        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000);
        return query.getResultList();
    }
    */

    public List<Order> findAllWithMemberDelivery() {
        /*
            Order 한 번 을 끌어올때 그 안의 member와 delivery 모두 한번에 끌어올 수 있도록 설계
            fetch는 사실 sql에 있는 문법은 아니고 jpa에서 제공하는 문법이다
        */
        return em.createQuery(
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class
        ).getResultList();
    }

    /*
     v4, SimpleOrderQueryDto는 OrderSimpleApiController 에 있는 SimpleOrderDto와 같은 형식이다
     다만 OrderRepository에 findOrderDtos함수가 다시 Controller를 참조하게 되는 현상을 방지하기 위해서
     repository에 같은 내용의 SimpleOrderQueryDto를 생성하였다

     그러나 엔티티를 조회하는 것이 아닌 Dto를 조회하는 로직은 리퍼지토리가 아니라 따로 클래스를 생성해서 그 안에서 처리해주기도 한다
     finOrderDtos 메서드는 굳이 여기서 짜지 않아도 됨
    */
    public List<OrderSimpleQueryDto> findOrderDtos() {
        return em.createQuery(
                "select new jpabook.jpashop.repository.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status, d.address)" +
                        " from Order o" +
                        " join o.member m" +
                        " join o.delivery d", OrderSimpleQueryDto.class
        ).getResultList();
    }

    /* OrderApiController */

    /*
     version 3
     일대 다 관계의 속성들을 join 시킨다
     -> order에 a,b
        orderItems에  a가 주문한 x,y  b 가 주문한 w,z 가 있을 경우에
        이들이 join 될 경우 order의 a와 b가 한번씩 더 찍혀서 나온다
    */
    public List<Order> findAllWithItem() {
        /*
         jpa에서의 distinct는 완전히 똑같은 row의 중복만 제거하는 데이터베이스와는 달리
         order_id(아래의 경우) 값이 중복되면 알아서 중복을 방지해주는 그런 기능이 있다
         따라서
         1) 데이터베이스에 distinct 키워드를 날린다
         2) 엔티티의 중복을 필터링하여 colletion에 담아준다
         의 두가지 기능이 존재한다

         치명적인 단점
         .setFirstResult(1)
         .setMaxResults(100)
         와 같이 페이징 쿼리를 하면 안된다

         왜?? -> 페치 조인이 되는 순간 우리가 원하는 row를 가져올 수 없기 때문
         위의 예를 다시 들어보면 row a가 2개, row b가 2개 인 상황에서 정상적으로 a또는 b를 리턴할 수 없게된다
         a두개가 리턴되는 경우, b두개가 리턴되는 경우, a하나 b두개가 리턴되는 경우 이런식으로 될 수 있음다

         이 모든 과정을 디비가 아닌 메모리로 올려서 페이징을 실행하기 때문에
         메모리 오버플로우가 날 수 있음
        */
        return em.createQuery(
                "select distinct o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d" +
                        " join fetch o.orderItems oi" +
                        " join fetch oi.item i", Order.class)
                .getResultList();
    }

    /*
     version 3-page 전용 페이징이 가능한 메서드
     추가된 부분 : default_batch_fetch_size: 100
     각각의 인쿼리에 대해서 100개의 row만큼 한번에 당겨오는 효과를 가짐 임
     --> 요 인쿼리가 테이블을 cross product시키지 않고 가져올 수 있는 핵심임

     만약 하나의 order에 1000개의 orderItem이 존재한다면
     fetch_size가 100이므로 10번 쿼리를 통해서 가져오게 된다
    */
    public List<Order> findAllWithMemberDelivery(int offset, int limit) {
        return em.createQuery(
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }


}




