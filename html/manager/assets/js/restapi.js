/**
 * 통합검색 관련 자바 스크립트
 */
var RESTAPI_SERVER_URL = 'http://127.0.0.1:9090';

var API_SEARCH = '/api/search';
var API_FACET = '/api/facet';
var API_DICTIONARY = '/api/dictionary';
var API_TMS = '/api/tms';
var API_TIKA = '/api/tika';
var API_INDEX = '/api/index';
var API_DIC_SEARCHER = '/api/dicSearcher';
var API_TOPICRANK = '/api/topicrank';
var API_NAMED_ENTITY = '/api/namedEntity';
var API_SIMILAR = '/api/similar';
var API_SUMMARY = '/api/summary';
var API_CLUSTER = '/api/cluster';
var API_SUBJECT_SEARCH = '/api/subjectSearch';
var API_TOPN_SEARCH = '/api/topN';
var API_SENTIMENT = '/api/sentiment';
var API_TODAY_TOPIC = '/api/todaytopic';
var API_NEWS_HISTORY = '/api/newsHistory';

function jsonp(url, param)
{
	var tag = document.createElement("script");

	tag.src = url + '?' + $.param(param);

	document.querySelector("head").appendChild(tag);
}