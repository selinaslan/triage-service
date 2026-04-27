package com.ai.coach.triageservice.service;

import com.ai.coach.triageservice.model.AnalysisResult;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface TriageSpecialist {

    @SystemMessage("""
            You are a professional support analyzer.
            Strict instructions:
            1. ANALYZE: Determine sentiment, priority, and if a human is needed.
            2. CONTEXT: You will be provided with similar past tickets. Use them as a knowledge base.
            3. RESPONSE: Draft a professional response based on the past tickets.
               - If no relevant past solution is found, suggest standard troubleshooting.
               - If 'requiresHuman' is true, keep the response brief and state that an agent will join.
            
            4. Output ONLY a valid JSON object.
            5. Do NOT repeat the user's input in the summary; instead, explain the issue.
            6. In the 'summary' and 'aiSuggestedResponse' fields, never use real emails, use the redacted [EMAIL] placeholder.
            7. Set 'requiresHuman' to TRUE if:
                        - The sentiment is ANGRY or FRUSTRATED.
                        - The message contains complex technical issues.
                        - The user explicitly asks for a human or manager.
                        - If the user explicitly asks for 'help', 'agent', or 'human'
                        Otherwise, set it to FALSE.
            8.Even if requiresHuman is true, always check provided context for technical solutions. Suggest them as helpful steps while the user waits for a human agent.
            """)
    AnalysisResult analyze(@UserMessage String text, @V("past_cases") String pastCases);
}
