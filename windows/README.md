# JAY for Windows

JAY is now being developed as a Windows-only desktop assistant in `windows/`.

## Current capabilities

- Futuristic dark desktop interface
- Text conversation
- Windows voice input using the default microphone
- Windows text-to-speech
- Opens approved/common applications and websites
- Local IPv4 discovery
- Internet connectivity test with latency
- Basic Windows system information
- No destructive system action is performed silently

## Build

Requirements:

- Windows 10 or Windows 11
- .NET 8 SDK
- A microphone is optional; it is only needed for voice input

From the `windows` directory:

```powershell
dotnet restore
dotnet build --configuration Release
dotnet run
```

For a self-contained 64-bit Windows package:

```powershell
dotnet publish --configuration Release --runtime win-x64 --self-contained true --output publish
```

## Architecture

`MainWindow.xaml` / `MainWindow.xaml.cs` — desktop UI, conversation and voice.

`JayCore.cs` — local command understanding and diagnostics.

`JayActionExecutor.cs` — controlled Windows actions.

The project intentionally starts lightweight. More powerful AI, persistent memory, wake-word detection, background operation, automation and security tooling will be added behind explicit permission boundaries rather than granting Jay unrestricted computer control.
