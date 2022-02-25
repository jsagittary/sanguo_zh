package com.gryphpoem.game.zw.resource.util;

import com.gryphpoem.game.zw.dataMgr.StaticChatDataMgr;
import com.gryphpoem.game.zw.resource.domain.s.StaticBlackWords;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ChenKui
 * @version 创建时间：2016-4-16 下午3:01:22
 * @declare
 */

public class ChatHelper {

	/**
	 * 是否是中文
	 *
	 * @param c
	 * @return
	 */
	public static boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
			return true;
		}
		return false;
	}

	public static boolean isCorrect(String correct) {
		String regEx = "[^`~!@#$%ㄒ^&*╮╭3ε≧▽≦‖｜￣∶丶·§№☆★ 	○●◎◇◆□■△▲※→←↑↓〓＃＆＠＼＾＿┌┍┎┏┐┑┒┓—┄┈├┝┞┟┠┡┢┣|┆┊┬┭┮┯┰┱┲┳┼┽┾┿╀╂╁╃≈≡≠＝≤≥＜＞≮≯∷±＋－×÷／∫∮∝∞∧∨∑∏∪∩∈∵∴⊥‖∠⌒⊙≌∽√ⅠⅡⅢⅣⅤⅥⅦⅧⅨⅩⅪⅫ①②③④⑤⑥⑦⑧⑨⑩︻︼︽︾〒↑↓☉⊙●〇◎¤★☆■▓「」『』◆◇▲△▼▽◣◥◢◣◤ ◥№↑↓→←↘↙Ψ※㊣∑⌒∩【】〖〗＠ξζω□∮〓※》∏卐√ ╳々♀♂∞①ㄨ≡╬╭╮╰╯╱╲ ▂ ▂ ▃ ▄ ▅ ▆ ▇ █ ▂▃▅▆█ ▁▂▃▄▅▆▇█▇▆▅▄▃▂▁╰╯∩(（）)+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：\"”“’。，、？\\p{P}a-zA-Z0-9\\u4e00-\\u9fa5\\x{0400}-\\x{052f}\\u0800-\\u4e00 àâäèéêëîïôœùûüÿçÀÂÄÈÉÊËÎÏÔŒÙÛÜŸÇ]+";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(correct);
		if (m.find())
			return true;
		else
			return false;
	}

	/**
	 * 比较若干条字符串是否相似
	 *
	 * @param contents
	 * @param rate
	 * @return 返回期中有几条文字是相似的 0到1
	 */
	public static int isSamely(LinkedList<String> contents, float rate) {
		int samelyCount = 0;
		String refer = contents.getLast();
		for (String string : contents) {
			if (compare(refer, string, rate)) {
				samelyCount++;
			}
			// refer = string;
		}
		return samelyCount;
	}

	/**
	 * @param rate 相似比率 0到1
	 * @return
	 */
	public static boolean compare(String str1, String str2, float rate) {
		if (str1 == null || str2 == null) {
			return false;
		}
		String longOne = null;
		String shortOne = null;
		if (str1.length() > str2.length()) {
			longOne = str1;
			shortOne = str2;
		} else {
			longOne = str2;
			shortOne = str1;
		}
		if (longOne.length() < 10) {//文字太短时增加判断的相似率值
			rate += (10 - longOne.length()) * 0.1;
			if (rate > 1) {
				rate = 1;
			}
		}
		int diffChar = 0;//为了性能 这里判断差别文字所占比率
		float longlength = longOne.length();
		float shortlength = shortOne.length();
		for (int i = 0; i < shortlength; i++) {
			char c = shortOne.charAt(i);
			if (!longOne.contains(String.valueOf(c))) {
				diffChar++;
			}
			if ((shortlength - diffChar) / longlength < rate) {//除去差别文字 得到剩余文字为可能相同文字  可能相同文字所占比率低于 rate 则不相似
				return false;
			}
		}
		return true;
	}

	public static void main(String[] args) {
		System.out.println("isCorrect(\"한국어\") = " + isCorrect("한국어"));

		/*LinkedList<String> lastChats = new LinkedList<>();

		// lastChats.add("搜索薇欣公中浩《乐玩社》，体验百款满V游戏，不花钱也能玩的很爽，游戏玩出新高度");
		lastChats.add("春花秋月何时了？往事知多少。小楼昨夜又东风，故国不堪回首月明中。");
		lastChats.add("春花秋月何时了？往事知多少。小楼昨夜又东风，故国不堪回首月明中。");
		String content = "春花秋月何时了？往事知多少。小楼昨夜又东风，故国不堪回首月明中。";

		boolean compare = false;
		if (content != null && content.length() > ChatConst.CHECK_CHAT_MIN_CNT) {
			lastChats.offer(content);
			while (lastChats.size() > ChatConst.CHECK_CHAT_COMPARE_CNT.get(0)) {
				lastChats.remove();
			}
			compare = true;
		}

		if (compare && lastChats.size() >= ChatConst.CHECK_CHAT_COMPARE_CNT.get(0) && ChatHelper.isSamely(lastChats, ChatConst.CHECK_CHAT_RATE) >= ChatConst.CHECK_CHAT_COMPARE_CNT.get(1)) {
			System.out.println("屏蔽");
		}*/
	}

	public static int minMatchTYpe = 1;      //最小匹配规则
	public static int maxMatchType = 2;      //最大匹配规则

	public static String sensitive(String content){
		Set<String> set = getSensitiveWord(content, 1);
		for(String str:set){
			content = filterSensitiveWords(content,str);
		}
		return content;
	}

	


	/**
	 * 获取文字中的敏感词

	 * @param txt 文字
	 * @param matchType 匹配规则&nbsp;1：最小匹配规则，2：最大匹配规则
	 * @return
	 * @version 1.0
	 */
	public static Set<String> getSensitiveWord(String txt , int matchType){
		Set<String> sensitiveWordList = new HashSet<String>();

		for(int i = 0 ; i < txt.length() ; i++){
			int length = CheckSensitiveWord(txt, i, matchType);    //判断是否包含敏感字符
			if(length > 0){    //存在,加入list中
				sensitiveWordList.add(txt.substring(i, i+length));
				i = i + length - 1;    //减1的原因，是因为for会自增
			}
		}

		return sensitiveWordList;
	}

	/**
	 * 检查文字中是否包含敏感字符，检查规则如下：<br>

	 * @param txt
	 * @param beginIndex
	 * @param matchType
	 * @return 如果存在，则返回敏感词字符的长度，不存在返回0
	 * @version 1.0
	 */
	public static int CheckSensitiveWord(String txt,int beginIndex,int matchType){
		boolean  flag = false;    //敏感词结束标识位：用于敏感词只有1位的情况
		int matchFlag = 0;     //匹配标识数默认为0
		char word = 0;
		Map nowMap = StaticChatDataMgr.getBlackWordsMap();
		for(int i = beginIndex; i < txt.length() ; i++){
			word = txt.charAt(i);
			nowMap = (Map) nowMap.get(word);     //获取指定key
			if(nowMap != null){     //存在，则判断是否为最后一个
				matchFlag++;     //找到相应key，匹配标识+1 
				if("1".equals(nowMap.get("isEnd"))){       //如果为最后一个匹配规则,结束循环，返回匹配标识数
					flag = true;       //结束标志位为true   
					if(minMatchTYpe == matchType){    //最小规则，直接返回,最大规则还需继续查找
						break;
					}
				}
			}
			else{     //不存在，直接返回
				break;
			}
		}
		if(matchFlag < 1 || !flag){        //长度必须 
			matchFlag = 0;
		}
		return matchFlag;
	}
	

	/**
	 * 过滤字符串中的敏感词汇
	 * @param content   文本
	 * @param sensitiveWord   敏感词汇
	 * @return
	 */
	public static String filterSensitiveWords(String content, String sensitiveWord) {

		if (content == null || sensitiveWord == null) {
			return content;
		}

		//获取和敏感词汇相同数量的星号
		String starChar = getStarChar(sensitiveWord.length());

		//替换敏感词汇
		return content.replace(sensitiveWord, starChar);
	}

	//大部分敏感词汇在10个以内，直接返回缓存的字符串
	public static String[] starArr={"*","**","***","****","*****","******","*******","********","*********","**********"};

	/**
	 * 生成n个星号的字符串
	 * @param length
	 * @return
	 */
	private static String getStarChar(int length) {
		if (length <= 0) {
			return "";
		}
		//大部分敏感词汇在10个以内，直接返回缓存的字符串
		if (length <= 10) {
			return starArr[length - 1];
		}

		//生成n个星号的字符串
		char[] arr = new char[length];
		for (int i = 0; i < length; i++) {
			arr[i] = '*';
		}
		return new String(arr);
	}

}
