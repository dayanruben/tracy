package ai.jetbrains.tracy.eval.utils

/**
 * An encapsulation of the AI feature under test.
 */
interface Generator<AIInputT: AIInput, AIOutputT: AIOutput> {
    suspend fun generate(input: AIInputT): AIOutputT
}
