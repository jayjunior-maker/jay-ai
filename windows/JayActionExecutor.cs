using System.Diagnostics;
using System.Windows;

namespace Jay.Windows;

public sealed class JayActionExecutor
{
    public string ExecuteOpen(string target)
    {
        try
        {
            Process.Start(new ProcessStartInfo
            {
                FileName = target,
                UseShellExecute = true
            });
            return $"Opened {target}, Sir.";
        }
        catch (Exception ex)
        {
            return $"I could not open {target}: {ex.Message}";
        }
    }

    public bool ConfirmDestructiveAction(string description)
    {
        var result = MessageBox.Show(
            $"Jay is requesting permission to {description}.\n\nThis action may change or delete data. Continue?",
            "Jay permission required",
            MessageBoxButton.YesNo,
            MessageBoxImage.Warning,
            MessageBoxResult.No);
        return result == MessageBoxResult.Yes;
    }
}
