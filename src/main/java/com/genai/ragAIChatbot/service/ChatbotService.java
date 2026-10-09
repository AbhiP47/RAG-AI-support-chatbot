package com.genai.ragAIChatbot.service;

import jakarta.annotation.PostConstruct;
import kotlin.reflect.jvm.internal.impl.descriptors.Visibilities;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatbotService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final EmbeddingModel embeddingModel;
    public ChatbotService(ChatClient chatClient, VectorStore vectorStore, ChatClient chatClient1, VectorStore vectorStore1, @Qualifier("googleGenAiTextEmbedding") EmbeddingModel embeddingModel) {

        this.chatClient = chatClient1;
        this.vectorStore = vectorStore1;
        this.embeddingModel = embeddingModel;
    }

    @Value("classpath*:/knowledge/*.pdf")
    private Resource[] policyFiles;

    @PostConstruct
    public void loadKnowledgeBase()
    {
        List<Document> allChunks = new ArrayList<>();
        TokenTextSplitter splitter = TokenTextSplitter
                .builder()
                .withChunkSize(300)
                .build();

        for(Resource resource : policyFiles)
        {
            PagePdfDocumentReader reader = new PagePdfDocumentReader(resource);
            List<Document> pages = reader.read();
            List<Document> chunks = splitter.apply(pages);
            allChunks.addAll(chunks);
        }

        vectorStore.add(allChunks);
    }
}
