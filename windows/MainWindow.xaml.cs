using System.Speech.Recognition;
using System.Speech.Synthesis;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using System.Windows.Media;

namespace Jay.Windows;

public partial class MainWindow : Window
{
    private readonly JayCore _core = new();
    private readonly JayActionExecutor _actions = new();
    private readonly SpeechSynthesizer _speaker = new();
    private SpeechRecognitionEngine? _recognizer;

    public MainWindow()
    {
        InitializeComponent();
        _speaker.Rate = -1;
        AddMessage("Jay", "Good afternoon, Sir. Jay is running on Windows and ready.");
    }

    private void SendButton_Click(object sender, RoutedEventArgs e) => ProcessInput();

    private void InputBox_KeyDown(object sender, KeyEventArgs e)
    {
        if (e.Key == Key.Enter)
        {
            e.Handled = true;
            ProcessInput();
        }
    }

    private void ProcessInput()
    {
        var input = InputBox.Text.Trim();
        if (input.Length == 0) return;

        AddMessage("You", input);
        InputBox.Clear();

        if (JayCore.TryGetOpenTarget(input, out var target))
        {
            var reply = _actions.ExecuteOpen(target);
            AddMessage("Jay", reply);
            Speak(reply);
            return;
        }

        var answer = _core.Reply(input);
        AddMessage("Jay", answer);
        Speak(answer);
    }

    private void VoiceButton_Click(object sender, RoutedEventArgs e)
    {
        try
        {
            _recognizer?.Dispose();
            _recognizer = new SpeechRecognitionEngine();
            _recognizer.LoadGrammar(new DictationGrammar());
            _recognizer.SetInputToDefaultAudioDevice();
            _recognizer.SpeechRecognized += Recognizer_SpeechRecognized;
            AddMessage("Jay", "I'm listening, Sir.");
            Speak("I'm listening, Sir.");
            _recognizer.RecognizeAsync(RecognizeMode.Single);
        }
        catch (Exception ex)
        {
            AddMessage("Jay", $"Voice input is unavailable: {ex.Message}");
        }
    }

    private void Recognizer_SpeechRecognized(object? sender, SpeechRecognizedEventArgs e)
    {
        if (e.Result.Confidence < 0.45f) return;
        Dispatcher.Invoke(() =>
        {
            InputBox.Text = e.Result.Text;
            ProcessInput();
        });
    }

    private void Speak(string text)
    {
        try { _speaker.SpeakAsyncCancelAll(); _speaker.SpeakAsync(text); }
        catch { /* Voice failure must not crash Jay. */ }
    }

    private void AddMessage(string speaker, string message)
    {
        var title = new TextBlock
        {
            Text = speaker,
            FontWeight = FontWeights.Bold,
            Foreground = speaker == "Jay" ? (Brush)FindResource("JayAccent") : Brushes.White,
            Margin = new Thickness(0, 8, 0, 3)
        };
        var body = new TextBlock
        {
            Text = message,
            TextWrapping = TextWrapping.Wrap,
            FontSize = 15,
            Foreground = Brushes.WhiteSmoke,
            Margin = new Thickness(0, 0, 0, 10)
        };
        ConversationPanel.Children.Add(title);
        ConversationPanel.Children.Add(body);
    }

    protected override void OnClosed(EventArgs e)
    {
        _recognizer?.Dispose();
        _speaker.Dispose();
        base.OnClosed(e);
    }
}
