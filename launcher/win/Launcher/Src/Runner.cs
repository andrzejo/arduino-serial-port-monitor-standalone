using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Threading;

namespace Launcher
{
    public class Runner
    {
        public void Run(string path)
        {
            try
            {
                var output = new List<string>();

                using (var process = new Process())
                {
                    process.StartInfo.WindowStyle = ProcessWindowStyle.Hidden;
                    process.StartInfo.CreateNoWindow = true;
                    process.StartInfo.FileName = "java";
                    process.StartInfo.UseShellExecute = false;
                    process.StartInfo.RedirectStandardOutput = true;
                    process.StartInfo.RedirectStandardError = true;
                    process.StartInfo.Arguments = $"-jar \"{path}\"";

                    void ReceivedEventHandler(object sender, DataReceivedEventArgs args)
                    {
                        var line = args.Data;
                        if (!string.IsNullOrWhiteSpace(line))
                        {
                            output.Add(line);
                        }
                    }

                    process.OutputDataReceived += ReceivedEventHandler;
                    process.ErrorDataReceived += ReceivedEventHandler;

                    process.Start();
                    process.BeginOutputReadLine();
                    process.BeginErrorReadLine();

                    while (!process.HasExited)
                    {
                        foreach (var s in output.ToArray())
                        {
                            if (s.Contains("---- APPLICATION STARTED ----"))
                            {
                                return;
                            }

                            Thread.Sleep(100);
                        }
                    }

                    if (process.ExitCode != 0)
                    {
                        var outputLines = string.Join("\n", output);
                        MessageBox.Show($"Failed to start application\nOutput: {output}\n\nError: {outputLines}");
                    }
                }
            }
            catch (Exception e)
            {
                Console.Error.WriteLine($"Exec failed {path} - {e}");
                MessageBox.Show($"Failed to execute JAR - {e.Message}");
            }
        }
    }
}