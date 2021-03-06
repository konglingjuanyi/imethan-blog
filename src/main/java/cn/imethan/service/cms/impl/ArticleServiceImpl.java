package cn.imethan.service.cms.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.google.common.collect.Lists;

import cn.imethan.common.jpa.EntityManagerSupport;
import cn.imethan.common.repository.DynamicSpecifications;
import cn.imethan.common.repository.SearchFilter;
import cn.imethan.dto.common.ReturnDto;
import cn.imethan.entity.cms.Article;
import cn.imethan.entity.cms.Channel;
import cn.imethan.entity.cms.Label;
import cn.imethan.repository.jpa.cms.ArticleRepository;
import cn.imethan.repository.jpa.cms.ChannelRepository;
import cn.imethan.service.cms.ArticleService;
import cn.imethan.service.cms.ChannelService;


/**
 * ArticleServiceImpl.java
 *
 * @author Ethan Wong
 * @time 2014年3月2日下午4:45:49
 */
@Service
@Transactional(readOnly = true)
public class ArticleServiceImpl extends EntityManagerSupport<Article, Long> implements ArticleService {
	
	private Logger logger = LogManager.getLogger(ArticleServiceImpl.class);
	
	@Autowired
	private ArticleRepository articleRepository;
	@Autowired
	private ChannelRepository channelRepository;
	@Autowired
	private ChannelService channelService;
	
	@Override
	public void test() {
		// TODO Auto-generated method stub
//		System.out.println("article test:"+this.entityManger.createQuery("from Article").getResultList());
//		System.out.println("article test:"+this.entityManger.find(Article.class, 23l));
		
		try {
			FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(super.entityManger);
			fullTextEntityManager.createIndexer().startAndWait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	public void testSearch() {
		FullTextEntityManager fullTextEntityManager = org.hibernate.search.jpa.Search.getFullTextEntityManager(entityManger);
		// entityManger.getTransaction().begin();

		// create native Lucene query unsing the query DSL
		// alternatively you can write the Lucene query using the Lucene query
		// parser
		// or the Lucene programmatic API. The Hibernate Search DSL is
		// recommended though
		QueryBuilder qb = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(Article.class).get();
		org.apache.lucene.search.Query luceneQuery = qb.keyword().onFields("title", "content").matching("imethan").createQuery();

		// wrap Lucene query in a javax.persistence.Query
		javax.persistence.Query jpaQuery = fullTextEntityManager.createFullTextQuery(luceneQuery, Article.class);

		// execute search
		List<Article> result = jpaQuery.getResultList();
		for(Article article:result){
			System.out.println(article.getTitle());
		}

		// entityManger.getTransaction().commit();
		// entityManger.close();
	}
	
	@Override
	@Transactional(readOnly = false)
	public ReturnDto saveOrModify(Article entity) {
		boolean isSuccess = true;
		String message = "保存成功";
		try {
			Channel channel = null;
			Long id = entity.getId();
			
			if(id !=null){
				//修改文章
				Article articleDb = this.getById(entity.getId());
				Channel channelDb = articleDb.getChannel();
				
				Channel channelNow = channelRepository.getOne(entity.getChannel().getId());
				if(channelDb.getId() != channelNow.getId()){
					//更新文章数量
					channelService.updateArticleAmount(channelDb.getId(),-1);
					channelService.updateArticleAmount(channelNow.getId(),+1);
					channel = channelNow;
				}else{
					channel = channelDb;
				}
				entity.setCreateTime(articleDb.getCreateTime());
				entity.setModifyTime(new Date());
				entity.setChannel(channel);
				articleRepository.save(entity);
				
			}else{
				//新增加文章
				
				//获取栏目信息
				channel = channelRepository.getOne(entity.getChannel().getId());
				
				//更新文章数量
				channelService.updateArticleAmount(channel.getId(),1);
				
				entity.setCreateTime(new Date());
				entity.setChannel(channel);
				articleRepository.save(entity);
			}

		} catch (Exception e) {
			e.printStackTrace();
			isSuccess = false;
			message = "保存失败";
			logger.error(e.getMessage());
		}
		return new ReturnDto(isSuccess , message , entity);
	}

	@Override
	public Article getById(Long id) {
		Article article = new Article();
		try {
			article = articleRepository.findOne(id);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
		return article;
	}

	@Override
	@Transactional(readOnly = false)
	public ReturnDto deleteById(Long id) {
		boolean isSuccess = true;
		String message = "删除成功";
		try {
			//更新文章数
			Article article = articleRepository.getOne(id);
			Channel channel = article.getChannel();
			
			//更新文章数量
			channelService.updateArticleAmount(channel.getId(),-1);
			
			articleRepository.delete(id);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
		return new ReturnDto(isSuccess , message);
	}
	
	/**
	 * 测试Specification查询
	 * @return
	 *
	 * @author Ethan Wong
	 * @datetime 2015年12月18日下午2:14:27
	 */
	public static Specification<Article> customer() {
		return new Specification<Article>() {
			public Predicate toPredicate(Root<Article> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = Lists.newArrayList();
				
//				List<Long> list = new ArrayList<Long>();
//				list.add(101l);
//				In in = builder.in(root.get("id"));
//				in.value(list);
				
				List<Label> list = new ArrayList<Label>();
				list.add(new Label(1l));
				In in = builder.in(root.get("labels"));
				in.value(list);
				
				return builder.and(in);
			}
		};
	}

	@Override
	public Page<Article> findPage(List<SearchFilter> filters, PageRequest pageable) {
		Page<Article> result = null;
		try {
			//如果没有登录不展现未发布文章
			if(!SecurityUtils.getSubject().isAuthenticated()){
		    	SearchFilter articleFilter2 = new SearchFilter("channel.isPublish",SearchFilter.Operator.EQ,true);
		    	filters.add(articleFilter2);
		    	
		    	SearchFilter articleFilter3 = new SearchFilter("isPublish",SearchFilter.Operator.EQ,true);
		    	filters.add(articleFilter3);
			}
	    	
			Specification<Article> spec = DynamicSpecifications.bySearchFilter(filters, Article.class);
			
			result = articleRepository.findAll(spec, pageable);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	@Transactional(readOnly = false)
	public ReturnDto updatePublish(Long id) {
		boolean flag = true;
		String message = "更新成功";
		boolean publish = true;
		if(this.getById(id).isPublish()){
			publish = false;
		};
		
		try {
			articleRepository.updatePublish(id,publish);
		} catch (Exception e) {
			e.printStackTrace();
			flag = false;
			message = "更新失败";
		}
		
		return new ReturnDto(flag,message);
	}

	@Override
	public List<Article> getTopCountArticleList(Integer count) {
		return articleRepository.findTop4ByIsPublish(true, new Sort(Direction.DESC,"id"));
	}
	
	private Article getNext(Long nextArticleId,Long channelId,Long nextCount,Long searchCount){
		Article next = articleRepository.findOne(nextArticleId);
		if(next != null && next.getChannelId().equals(channelId) && next.isPublish()){
			return next;
		}else{
			searchCount ++ ;
			if(searchCount > nextCount){
				return null;
			}
			return getNext(nextArticleId + 1l,channelId,nextCount,searchCount);
		}
	}
	
	private Article getPrev(Long prevArticleId,Long channelId,Long prevCount,Long searchCount){
		Article prev = articleRepository.findOne(prevArticleId);
		
		if(prev != null && prev.getChannelId().equals(channelId) && prev.isPublish()){
			return prev;
		}else{
			searchCount ++ ;
			if(searchCount > prevCount){
				return null;
			}
			return getPrev(prevArticleId - 1l,channelId,prevCount,searchCount);
		}
	}

	@Override
	public Article getArticleWithPrevAndNext(Long articleId) {
		
		if(StringUtils.isEmpty(articleId)){
			return null;
		}
		
		Article article = this.getById(articleId);
		if(article == null){
			return null;
		}
		
		//如果没有登录不展现未发布文章
		if(!SecurityUtils.getSubject().isAuthenticated() && !article.isPublish()){
			return null;
		}
		
		Long channelId = article.getChannelId();
		
		
		List<Article> lastArticles = articleRepository.findFirst1ByChannel(article.getChannel(), new Sort(Direction.DESC,"id"));
		Long lastArticleId = lastArticles.get(0).getId();
		
		//获取全部下一篇记录数
		Long nextCount = lastArticleId - article.getId();
		
		if(nextCount > 0){
			Long searchCount = 0l;
			Article next = this.getNext(articleId + 1l, channelId,nextCount,searchCount);
			article.setNext(next);
			
		}
		
		//获取全部上一篇记录数
		Long prevCount = lastArticleId - nextCount - 1;
		if(prevCount > 0){
			Long searchCount = 0l;
			Article prev = this.getPrev(articleId - 1l, channelId,prevCount,searchCount);
			article.setPrev(prev);
		}
		
		return article;
	}

	@Override
	public Long getIndexArticleCount() {
		
		List<SearchFilter> filters = Lists.newArrayList();
	    SearchFilter articleFilter3 = new SearchFilter("isPublish",SearchFilter.Operator.EQ,true);
	    filters.add(articleFilter3);    	
		Specification<Article> spec = DynamicSpecifications.bySearchFilter(filters, Article.class);
		
		return articleRepository.count(spec);
	}


}
