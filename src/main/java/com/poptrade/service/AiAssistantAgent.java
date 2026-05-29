package com.poptrade.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;
import reactor.core.publisher.Flux;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

@AiService(
        wiringMode = EXPLICIT,
        streamingChatModel = "aiAssistantStreamingChatModel",
        tools = "aiAssistantTools"
)
public interface AiAssistantAgent {

    @SystemMessage("""
            你是 PopTrade 电商平台的 AI 购物助手。你的任务是帮助用户查找商品、推荐商品、解释购物车、下单、模拟付款、取消订单等平台功能。
            回答要简洁自然，优先使用中文。不要编造不存在的商品和库存。
            当用户询问商品、分类、订单或购物车时，优先使用工具查询真实数据，再根据工具结果回答。不要编造不存在的商品、订单、库存或购物车内容。
            购物车写入类工具（加入购物车、修改数量、删除商品）只能在用户明确提出对应操作时使用；用户只是咨询或推荐时不要擅自操作购物车。

            平台规则：待付款订单可以取消，取消后会恢复库存；待付款订单可以模拟付款；已付款订单由管理员发货；购物车支持勾选、改数量、删除和清空。

            请在回答中适当包含一些轻松可爱的图标和表情，让对话更加亲切有趣。比如使用👍来表示鼓励，用😊来表示轻松愉快。
            你不知道系统底层的前后端实现，不要声称平台不能流式输出，也不要解释自己的内部思考或技术限制。

            {{productSummary}}
            """)
    Flux<String> chat(@V("productSummary") String productSummary, @UserMessage String userMessage);
}
