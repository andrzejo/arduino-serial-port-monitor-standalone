using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text.RegularExpressions;

namespace Launcher
{
    public class JarFinder
    {
        private readonly string _path;

        public JarFinder(string path)
        {
            _path = path;
        }

        public Dictionary<string, int> FindCandidates()
        {
            var list = new Dictionary<string, int>();
            const string pattern = "Arduino*Serial*Port*Monitor*Standalone*.jar";
            foreach (var file in Directory.GetFiles(_path, pattern))
            {
                var version = GetVersion(file);
                list[file] = version;
            }

            return list
                .OrderBy(x => x.Value)
                .ToDictionary(x => x.Key, x => x.Value);
        }

        private int GetVersion(string file)
        {
            var regex = new Regex(@"(?<major>\d+)\.(?<minor>\d+)\.(?<patch>\d+)");
            var name = Path.GetFileName(file);
            var match = regex.Match(name);
            if (match.Success && match.Groups.Count == 3)
            {
                var major = GetIntValue(match, "major");
                var minor = GetIntValue(match, "minor");
                var patch = GetIntValue(match, "patch");
                return major * 100 + minor * 10 + patch;
            }

            return 0;
        }

        private int GetIntValue(Match match, string group)
        {
            var value = match.Groups[group].Value;
            return int.TryParse(value, out var val) ? val : 0;
        }
    }
}