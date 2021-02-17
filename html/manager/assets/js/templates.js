Vue
        .component(
                'app-header',
                {
	                template : '<nav class="navbar navbar-dark fixed-top bg-dark flex-md-nowrap p-0 shadow">\
	        							<a class="navbar-brand col-sm-3 col-md-2 mr-0" href="#" id="companyName">(주) 솔트룩스</a>\
	        							<input class="form-control form-control-dark w-100" type="text" placeholder="Search" aria-label="Search">\
	        							<ul class="navbar-nav px-3">\
	        								<li class="nav-item text-nowrap"><a class="nav-link" href="#">Sign out</a></li>\
	        							</ul>\
	                				</nav>',
                });

Vue
        .component(
                'app-left-menu',
                {
	                template : '<ul class="nav flex-column">\
	                					<li class="nav-item"><a class="nav-link active" href="/manager/index.html"> <span data-feather="home"></span> Dashboard <span class="sr-only">(current)</span></a></li>\
	                					<li class="nav-item"><a class="nav-link" href="#"> <span data-feather="file"></span> 메뉴명 </a></li>\
					                	<h6 class="sidebar-heading d-flex justify-content-between align-items-center px-3 mt-4 mb-1 text-muted">\
											<span>색인관리</span> <a class="d-flex align-items-center text-muted" href="#"> <span data-feather="plus-circle"></span></a>\
										</h6>\
	                					<ul class="nav flex-column mb-2">\
	                						<li class="nav-item"><a class="nav-link" href="/manager/html/index/indexList.html"> <span data-feather="file-text"></span> 색인 현황 </a></li>\
	                					</ul>\
					                	<h6 class="sidebar-heading d-flex justify-content-between align-items-center px-3 mt-4 mb-1 text-muted">\
											<span>기능 샘플</span> <a class="d-flex align-items-center text-muted" href="#"> <span data-feather="plus-circle"></span></a>\
										</h6>\
										<ul class="nav flex-column mb-2">\
											<li class="nav-item"><a class="nav-link" href="/manager/html/sample/autocomplete-jquery.html"> <span data-feather="file-text"></span> 자동 완성 - jQuery </a></li>\
	                						<li class="nav-item"><a class="nav-link" href="/manager/html/sample/famous-jquery.html"> <span data-feather="file-text"></span> 인기 검색어 - jQuery </a></li>\
	                						<li class="nav-item"><a class="nav-link" href="/manager/html/sample/recent-jquery.html"> <span data-feather="file-text"></span> 최근 검색어 - jQuery </a></li>\
	                						<li class="nav-item"><a class="nav-link" href="/manager/html/sample/search-jquery.html"> <span data-feather="file-text"></span> 검색 - jQuery </a></li>\
	                						<li class="nav-item"><a class="nav-link" href="/manager/html/sample/facet-jquery.html"> <span data-feather="file-text"></span> 패싯 - jQuery </a></li>\
	                						<li class="nav-item"><a class="nav-link" href="/manager/html/sample/topicrank-jquery.html"> <span data-feather="file-text"></span> 토픽랭크 - jQuery </a></li>\
	                						<li class="nav-item"><a class="nav-link" href="/manager/html/sample/wordcloud-jquery.html"> <span data-feather="file-text"></span> 워드클라우드 - jQuery </a></li>\
	                						<li class="nav-item"><a class="nav-link" href="/manager/html/sample/named-entity-jquery.html"> <span data-feather="file-text"></span> 개체명분석 - jQuery </a></li>\
	                						<li class="nav-item"><a class="nav-link" href="/manager/html/sample/clustering-vue.html"> <span data-feather="file-text"></span> 클러스터링분석 - Vue </a></li>\
	                						<li class="nav-item"><a class="nav-link" href="/manager/html/sample/sentiment-jquery.html"> <span data-feather="file-text"></span> 감성분석 - jQuery </a></li>\
	                						<li class="nav-item"><a class="nav-link" href="/manager/html/sample/todaytopic-vue.html"> <span data-feather="file-text"></span> 오늘의 이슈 - Vue </a></li>\
	                						<li class="nav-item"><a class="nav-link" href="/manager/html/sample/news-history-jquery.html"> <span data-feather="file-text"></span> 뉴스 히스토리 - jQuery </a></li>\
										</ul>\
                						<h6 class="sidebar-heading d-flex justify-content-between align-items-center px-3 mt-4 mb-1 text-muted">\
	                						<span>사전관리</span> <a class="d-flex align-items-center text-muted" href="#"> <span data-feather="plus-circle"></span></a>\
	                					</h6>\
					        			<ul class="nav flex-column mb-2">\
		                					<li class="nav-item"><a class="nav-link" href="/manager/html/dictionary/autocomplete.html"> <span data-feather="file-text"></span> 자동완성 사전 </a></li>\
		                					<li class="nav-item"><a class="nav-link" href="/manager/html/dictionary/corekeyword.html"> <span data-feather="file-text"></span> 핵심어 사전 </a></li>\
		                					<li class="nav-item"><a class="nav-link" href="/manager/html/dictionary/statNotInclude.html"> <span data-feather="file-text"></span> 검색현황 제외 단어 </a></li>\
		                					<li class="nav-item"><a class="nav-link" href="/manager/html/dictionary/stopword.html"> <span data-feather="file-text"></span> 불용어 사전 </a></li>\
		                					<li class="nav-item"><a class="nav-link" href="/manager/html/dictionary/stopwordChunk.html"> <span data-feather="file-text"></span> 청커 불용어 사전 </a></li>\
		                					<li class="nav-item"><a class="nav-link" href="/manager/html/dictionary/stopwordFacet.html"> <span data-feather="file-text"></span> 패싯 불용어 사전 </a></li>\
		                					<li class="nav-item"><a class="nav-link" href="/manager/html/dictionary/stopwordPattern.html"> <span data-feather="file-text"></span> 토픽랭크 불용어 패턴 사전 </a></li>\
		                					<li class="nav-item"><a class="nav-link" href="/manager/html/dictionary/synonym.html"> <span data-feather="file-text"></span> 유의어 사전 </a></li>\
		                				</ul>\
		                			</ul>',
                });

var appHeader = new Vue({
	el: '#header'
});

var appLeftMenu = new Vue({
	el: '#left_menu'
});