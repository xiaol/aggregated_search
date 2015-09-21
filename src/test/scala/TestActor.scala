
// Created by ZG on 15/9/10.
//

import akka.actor.{ActorSystem, Props}

import nlps._
import cores._
import clients.media._
import clients.community._
import clients.searchengine._
import org.ansj.splitWord.analysis
import org.ansj.splitWord.analysis.NlpAnalysis



object TestActor extends App{

  implicit val system = ActorSystem("TestActor")

  val actorForTest = system.actorOf(Props[AggregateSearchActor], "AggregateSearchActor")
  actorForTest ! ExtractAggregateSearch("周杰伦")
  actorForTest ! ExtractAggregateSearch("桂纶镁")

//  val actorForTest = system.actorOf(Props[TemplateContentExtractActor], "TemplateContentExtractActor")
//  actorForTest ! ExtractContentByTemplate(
//    SearchItems(List(
//      SearchItem("http://eladies.sina.com.cn/news/star/2015-09-13/0739/doc-ifxhuyha2168458.shtml","比伯周杰伦发型丑哭了 明星发型黑历史 "),
//      SearchItem("http://www.sd.xinhuanet.com/news/yule/2015-09/13/c_1116522683.htm","周杰伦吴彦祖周华健宁静刘烨 大盘点明星混血子女"),
//      SearchItem("http://news.xinhuanet.com/overseas/2015-09/12/c_128222729.htm","汪峰撕媒体全还原:那英周杰伦收住笑容 "),
//      SearchItem("http://www.aihami.com/a/gongkai/xuexi/88560.html","Angelababy盼周杰伦来婚礼 自爆是“横店周杰伦”"),
//      SearchItem("http://game.91.com/chanye/news/21873820.html","周杰伦、蔡依林、吴奇隆代言游戏的时候会说什么"),
//      SearchItem("http://news.fznews.com.cn/fuzhou/20150913/55f4cb0c84978.shtml","杨幂小S柳岩周杰伦姚晨蓝洁瑛 明星遭辱幸酸往事"),
//      SearchItem("http://news.youth.cn/yl/201509/t20150912_7107270_2.htm","Angelababy婚礼欲邀周杰伦 谈黄晓明:领证是承诺"),
//      SearchItem("http://www.ahtv.cn/c/2015-09-12/597441.html","-TFBOYS排舞曝光 周杰伦探班大请客"),
//      SearchItem("http://365jia.cn/newshop/news/254176/news_id/38458","如果周杰伦参与设计纳智捷家族车型……"),
//      SearchItem("http://www.chinatimes.com/cn/realtimenews/20150912004027-260404","《中国好声音4》台首播 周董学员获封嘻哈女王"),
//      SearchItem("http://ln.qq.com/a/20150912/030589.htm","今晚周杰伦奥体开场 全场一片粉红"),
//      SearchItem("http://hb.people.com.cn/n/2015/0912/c192237-26349061-3.html","中国好声音第四季周杰伦四强名单出炉 那英看好周杰伦夺冠"),
//      SearchItem("http://news.syd.com.cn/system/2015/09/12/010818232.shtml","堵堵堵!去看周杰伦演唱会的亲注意啦"),
//      SearchItem("http://gx.people.com.cn/n/2015/0912/c229142-26347887.html","吴宗宪怒呛周杰伦泛泪花 揭明星好友翻脸原因"),
//      SearchItem("http://mt.sohu.com/20150912/n420976383.shtml","周杰伦当爸变胆小登台紧张冒汗"),
//      SearchItem("http://news.chinabyte.com/45/13546045.shtml","王思聪做直播PandaTV周董代言 小智跳槽斗鱼危机"),
//      SearchItem("http://www.china.com.cn/ent/2015-09/12/content_36568540.htm","吴彦祖宁静刘烨周杰伦 明星混血子女大盘点(图)"),
//      SearchItem("http://ent.china.com.cn/live/2015-09/12/content_34234344.htm","Angelababy盼周杰伦来婚礼 陈晓回应恋情"),
//      SearchItem("http://ent.china.com.cn/live/2015-09/12/content_34233994.htm","Angelababy婚礼欲邀周杰伦谈黄晓明:领证是承诺"),
//      SearchItem("http://news.youth.cn/yl/201509/t20150911_7106939.htm","沈阳警方抓获周杰伦演唱会假票团伙 已售20余张"),
//      SearchItem("http://roll.sohu.com/20150911/n420937103.shtml","刘畊宏和周杰伦什么关系 刘畊宏周杰伦的英雄账号是什么"),
//      SearchItem("http://www.chinadaily.com.cn/hqcj/zgjj/2015-09-11/content_14178521.html","周杰伦告北京通州一酒楼擅用婚照 索赔110万元"),
//      SearchItem("http://e.gmw.cn/2015-09/10/content_16997201.htm","《大牌驾到》任贤齐自曝曾遭周杰伦“挖墙脚”"),
//      SearchItem("http://ent.china.com.cn/live/2015-09/10/content_34215737.htm","周杰伦演唱会干扰国足歌迷澄清反击盘点明星开唱风波"),
//      SearchItem("http://ent.qq.com/a/20150910/017771.htm","昆凌产后与周杰伦同地捞金 工作太忙难见面"),
//      SearchItem("http://ent.sina.com.cn/y/ygangtai/2015-09-10/doc-ifxhupin3442543.shtml","周杰伦昆凌同地工作 安排太紧没时间见面"),
//      SearchItem("http://ent.163.com/15/0910/08/B34VPL5B00031H2L.html","昆凌与周杰伦同地工作 因安排太紧没时间见面"),
//      SearchItem("http://ent.163.com/15/0909/18/B33EOG6S00031H2L.html","巫启贤与刘文正解师徒恩怨 周杰伦躺枪"),
//      SearchItem("http://ent.sina.com.cn/y/ygangtai/2015-09-09/doc-ifxhqhui5004558.shtml","巫启贤与刘文正解师徒怨 周杰伦躺枪"),
//      SearchItem("http://ent.huanqiu.com/star/mingxing-gangtai/2015-09/7449207.html","周杰伦给足球赛添堵？歌迷反驳：他合同在先"),
//      SearchItem("http://roll.sohu.com/20150909/n420731509.shtml","婚照被擅作宣传 周杰伦起诉酒楼索赔百万"),
//      SearchItem("http://ent.163.com/15/0909/09/B32GDPSJ00031H2L.html","演唱会舞台干扰足球迷观战 周杰伦拆台迁就"),
//      SearchItem("http://e.gmw.cn/2015-09/09/content_16975762.htm","周杰伦执导宣传片 开水上摩托艇"),
//      SearchItem("http://ent.qq.com/a/20150909/014394.htm","周杰伦演唱会舞台干扰足球迷观战 挨骂粉丝反击"),
//      SearchItem("http://ent.people.com.cn/n/2015/0909/c1012-27560746.html","周杰伦起诉酒楼索赔百万 被告墙面挂其巨幅结婚照"),
//      SearchItem("http://yule.sohu.com/20150909/n420694890.shtml","周杰伦演唱会干扰国足？歌迷澄清：早审批"),
//      SearchItem("http://yule.sohu.com/20150910/n420813953.shtml","《大牌驾到》任贤齐自曝曾遭 周杰伦 “挖墙脚”？"),
//      SearchItem("http://fj.qq.com/a/20150909/016302.htm","周杰伦 演唱会舞台干扰足球迷观战 掀起粉丝骂战"),
//      SearchItem("http://yule.sohu.com/20150909/n420694890.shtml","周杰伦 演唱会干扰国足？歌迷澄清：早审批"),
//      SearchItem("http://cd.qq.com/a/20150910/018769.htm","昆凌产后与 周杰伦 同地捞金 工作太忙难见面"),
//      SearchItem("http://music.yule.sohu.com/20150908/n420631019.shtml","SNH48首支原创单曲问世 周杰伦 五月天团队护航"),
//      SearchItem("http://cq.qq.com/a/20150907/007646.htm","王思聪：没想到 周杰伦 “爆”了我"),
//      SearchItem("http://news.sohu.com/20150906/n420476875.shtml","周杰伦 LOL表演赛完爆王思聪 林更新被深深折服"),
//      SearchItem("http://cd.qq.com/a/20150909/026649.htm","演唱会舞台干扰足球迷观战 周杰伦 拆台迁就"),
//      SearchItem("http://music.yule.sohu.com/20150906/n420491289.shtml","周杰伦 王思聪LOL表演赛 小公举秀翻全场"),
//      SearchItem("http://ah.people.com.cn/n/2015/0912/c358331-26348865.html","全程还原汪峰＂手撕媒体＂：那英 周杰伦 收住笑容"),
//      SearchItem("http://www.tibet.cn/news/roll/1442101400137.shtml","全程还原汪峰\"撕\"媒体:那英周杰伦收住笑容"),
//      SearchItem("http://news.xinhuanet.com/overseas/2015-09/12/c_128222729.htm","汪峰撕媒体全还原:那英周杰伦收住笑容"),
//      SearchItem("http://qh.people.com.cn/n/2015/0913/c182762-26355387.html","Angelababy婚礼欲邀周杰伦 谈黄晓明:领证是承诺"),
//      SearchItem("http://gb.cri.cn/27564/2015/09/08/3465s5094607.htm","揭秘明星网游大神 赵丽颖周杰伦王思聪林更新刘烨"),
//      SearchItem("http://d.youth.cn/shrgch/201509/t20150912_7108245.htm","baby盼周杰伦出席婚礼 陈晓回应恋情"),
//      SearchItem("http://www.ce.cn/celt/wyry/201509/07/t20150907_6408725.shtml","周杰伦为学员对战选自己的歌 网友:周董当导师偷懒"),
//      SearchItem("http://www.taiwan.cn/xwzx/pt/201509/t20150910_10641460.htm","巩俐王志文陈好周杰伦 细数娱乐圈的麻辣教师们"),
//      SearchItem("http://ent.people.com.cn/n/2015/0912/c1012-27574938.html","baby婚礼想请偶像周杰伦 现实生活中相信诺言"),
//      SearchItem("http://gb.cri.cn/27224/2015/09/12/1042s5099643.htm","Angelababy盼周杰伦来婚礼 陈晓回应恋情(组图)"),
//      SearchItem("http://www.chinaz.com/visit/2015/0909/444214.shtml","“失败者”周杰伦：跨界潜规则 "),
//      SearchItem("http://www.kuwo.cn/mingxing/%E5%91%A8%E6%9D%B0%E4%BC%A6/","【周杰伦】"),
//      SearchItem("http://baike.haosou.com/doc/2526484-2669235.html","周杰伦"),
//      SearchItem("http://www.kugou.com/yy/singer/home/3520.html","周杰伦"),
//      SearchItem("http://tool.xdf.cn/jdyl/result_zhoujielun.html?utm_source=bing%26utm_medium=organic%26utm_campaign=seohezuo-bing","周杰伦经典语录"),
//      SearchItem("http://ent.sina.com.cn/y/ygangtai/2015-09-08/doc-ifxhqhun8528059.shtml","周杰伦演唱会干扰国足？歌迷澄清：早审批|周杰伦|演唱会 ..."),
//      SearchItem("http://s.music.so.com/singer?id=442","周杰伦的歌曲"),
//      SearchItem("http://www.9ku.com/geshou/798.htm","【周杰伦歌曲大全】周杰伦的歌"),
//      SearchItem("http://www.yinyuetai.com/fanclub/154","【周杰伦 (JayChou)")
//  ))
//  )


  Thread.sleep(1000 * 70)
  system.shutdown()

}
