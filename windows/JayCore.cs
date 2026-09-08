using System.Diagnostics;
using System.Net.NetworkInformation;
using System.Text;

namespace Jay.Windows;

public sealed class JayCore
{
    public string Reply(string input)
    {
        var text = input.Trim();
        if (text.Length == 0) return "I'm listening, Sir.";

        var lower = text.ToLowerInvariant();
        if (lower is "hello" or "hi" or "hey" || lower.Contains("how are you"))
            return "Good to hear from you, Sir. Jay is online and ready.";
        if (lower.Contains("who are you") || lower.Contains("what are you"))
            return "I'm Jay, your Windows AI assistant. I can talk with you and perform controlled Windows tasks.";
        if (lower.Contains("help") || lower.Contains("what can you do"))
            return "Try: open Chrome, open Notepad, show my IP, check my internet, show system info, or what time is it.";
        if (lower.Contains("time")) return $"It is {DateTime.Now:h:mm tt}, Sir.";
        if (lower.Contains("date")) return $"Today is {DateTime.Now:dddd, MMMM d, yyyy}, Sir.";
        if (lower.Contains("my ip") || lower.Contains("ip address")) return GetLocalIp();
        if (lower.Contains("internet") || lower.Contains("connection")) return CheckInternet();
        if (lower.Contains("system info") || lower.Contains("computer info")) return GetSystemInfo();

        return "I understand the request, Sir, but that capability is not implemented yet. I will not pretend that it is.";
    }

    private static string GetLocalIp()
    {
        foreach (var adapter in NetworkInterface.GetAllNetworkInterfaces())
        {
            if (adapter.OperationalStatus != OperationalStatus.Up) continue;
            foreach (var address in adapter.GetIPProperties().UnicastAddresses)
            {
                if (address.Address.AddressFamily == System.Net.Sockets.AddressFamily.InterNetwork &&
                    !System.Net.IPAddress.IsLoopback(address.Address))
                    return $"Your local IPv4 address is {address.Address}.";
            }
        }
        return "I could not find an active local IPv4 address.";
    }

    private static string CheckInternet()
    {
        try
        {
            using var ping = new Ping();
            var reply = ping.Send("1.1.1.1", 2500);
            return reply.Status == IPStatus.Success
                ? $"Internet connectivity is working. Ping to 1.1.1.1 was {reply.RoundtripTime} ms."
                : $"The internet test failed: {reply.Status}.";
        }
        catch (Exception ex)
        {
            return $"I could not complete the internet test: {ex.Message}";
        }
    }

    private static string GetSystemInfo()
    {
        var os = Environment.OSVersion.VersionString;
        var machine = Environment.MachineName;
        var cpu = Environment.ProcessorCount;
        var ram = GC.GetGCMemoryInfo().TotalAvailableMemoryBytes / (1024d * 1024d * 1024d);
        return $"Computer: {machine}. OS: {os}. CPU threads: {cpu}. Available managed memory estimate: {ram:F1} GB.";
    }

    public static bool TryGetOpenTarget(string input, out string target)
    {
        target = string.Empty;
        var lower = input.Trim().ToLowerInvariant();
        var known = new Dictionary<string, string>
        {
            ["notepad"] = "notepad.exe",
            ["calculator"] = "calc.exe",
            ["calc"] = "calc.exe",
            ["file explorer"] = "explorer.exe",
            ["explorer"] = "explorer.exe",
            ["command prompt"] = "cmd.exe",
            ["cmd"] = "cmd.exe",
            ["powershell"] = "powershell.exe"
        };

        foreach (var pair in known)
        {
            if (lower.Contains("open " + pair.Key) || lower == pair.Key)
            {
                target = pair.Value;
                return true;
            }
        }

        if (lower.Contains("open chrome")) { target = "https://www.google.com"; return true; }
        if (lower.Contains("open edge")) { target = "https://www.microsoft.com/edge"; return true; }
        if (lower.Contains("open github")) { target = "https://github.com"; return true; }
        return false;
    }
}
