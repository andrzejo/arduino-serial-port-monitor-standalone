using System;
using System.Runtime.InteropServices;

namespace Launcher
{
    public static class MessageBox
    {
        [DllImport("User32.dll", EntryPoint = "MessageBox", CharSet = CharSet.Auto)]
        private static extern int MsgBox(IntPtr hWnd, string lpText, string lpCaption, uint uType);

        public static void Show(string msg)
        {
            MsgBox(IntPtr.Zero, msg, "Arduino Serial Port Monitor Launcher Error", 0x00000010);
        }
    }
}