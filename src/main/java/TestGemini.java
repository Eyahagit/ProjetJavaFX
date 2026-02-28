import Services.ServiceGemini;

public class TestGemini {
    public static void main(String[] args) {
        System.out.println("🔍 Test de l'API Gemini...");
        ServiceGemini service = new ServiceGemini();
        String reponse = service.sendPrompt("Dis bonjour en français");
        System.out.println("📝 Réponse: " + reponse);
    }
}