using System;
using System.IO;
using System.Net;
using System.Text;

namespace Launcher
{
    public static class Api
    {
        public static void Request(string method, string endpoint, string arg)
        {
            const string address = "http://localhost:4255/api/";
            var request = WebRequest.Create(address + endpoint);
            request.Method = method;
            try
            {
                var encoding = new ASCIIEncoding();
                if (!string.IsNullOrEmpty(arg))
                {
                    var body = encoding.GetBytes(arg);
                    request.GetRequestStream().Write(body, 0, body.Length);
                }

                var response = (HttpWebResponse)request.GetResponse();
                Console.WriteLine($"HTTP status: {response.StatusCode}");

                var stream = response.GetResponseStream();
                var responseBody = stream == null ? null : new StreamReader(stream).ReadToEnd();
                Console.WriteLine("Response:");
                Console.WriteLine("--------------------------");
                Console.WriteLine(responseBody);
                Console.WriteLine("--------------------------");
            }
            catch (Exception e)
            {
                Console.WriteLine($"API request failed: {e.Message}");
            }
        }
    }
}