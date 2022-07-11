using System;
using System.Collections.Generic;
using System.Linq;

namespace Launcher
{
    internal static class Program
    {
        private static void Main(string[] args)
        {
            if (HandleApi(args))
            {
                return;
            }

            var appPath = AppDomain.CurrentDomain.BaseDirectory;
            var finder = new JarFinder(appPath);
            var candidates = finder.FindCandidates();
            if (candidates.Count == 0)
            {
                MessageBox.Show($"Unable to find any Arduino Serial Port Monitor *.jar in Launcher directory.\nPath: {appPath}");
                return;
            }

            var path = candidates.First().Key;
            var runner = new Runner();
            runner.Run(path);
        }

        private static bool HandleApi(IReadOnlyList<string> args)
        {
            if (args.Count > 2 && args[0] == "api")
            {
                var method = args[1];
                var endpoint = args[2];
                var arg = (args.Count > 3) ? args[3] : "";
                Api.Request(method, endpoint, arg);
                return true;
            }

            return false;
        }
    }
}