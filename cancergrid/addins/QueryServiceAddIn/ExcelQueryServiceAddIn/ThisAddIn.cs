using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Xml.Linq;
using Excel = Microsoft.Office.Interop.Excel;
using Office = Microsoft.Office.Core;

namespace ExcelQueryServiceAddIn
{
    public partial class ThisAddIn
    {
        private ExcelQueryServiceControl queryServiceControl;
        private Microsoft.Office.Tools.CustomTaskPane myCustomTaskPane;
        private Office.CommandBarButton XMLUnmapButton;
        private Office.CommandBarButton CellUnmapButton;
        private Office.CommandBarButton ClearInListButton;

        private void ThisAddIn_Startup(object sender, System.EventArgs e)
        {
            AddUnmapButtonMenuCommand();
        }

        private void ThisAddIn_Shutdown(object sender, System.EventArgs e)
        {
            RemoveQueryServiceTaskPane();
        }

        public void AddUnmapButtonMenuCommand()
        {
            Office._CommandBarButtonEvents_ClickEventHandler xButtonHandler =
                new Office._CommandBarButtonEvents_ClickEventHandler(unmapXMLClick);

            Office._CommandBarButtonEvents_ClickEventHandler cButtonHandler =
                new Office._CommandBarButtonEvents_ClickEventHandler(unmapHeaderClick);

            Office._CommandBarButtonEvents_ClickEventHandler lButtonHandler =
                new Office._CommandBarButtonEvents_ClickEventHandler(unmapListClick);


            //Uncomment and Loop to find correct command dropdown menu.
            /*
            Office.CommandBars commandBars;

            commandBars = (Office.CommandBars)Application.CommandBars;

            foreach (Office.CommandBar commandBar in commandBars)
            {
                string name = commandBar.Name;
                string iName = commandBar.Name;
                try
                {
                    Office.CommandBarButton testButton = (Office.CommandBarButton)Application.CommandBars[commandBar.Name].Controls.Add(Office.MsoControlType.msoControlButton, missing, missing, missing, true);
                    testButton.Caption = commandBar.Name;
                    testButton.Visible = true;
                    //Uncomment when you want to clear the junk you just added.
                    Application.CommandBars[commandBar.Name].Reset();
                }
                catch (Exception e)
                { }
            }
            */

            ClearInListButton = (Office.CommandBarButton)Application.CommandBars["List Range Popup"].Controls.Add(Office.MsoControlType.msoControlButton, missing, missing, missing, true);
            ClearInListButton.Click += lButtonHandler;
            ClearInListButton.Caption = "Remove Mapping";
            ClearInListButton.Visible = true;

            XMLUnmapButton = (Office.CommandBarButton)Application.CommandBars["XML Range Popup"].Controls.Add(Office.MsoControlType.msoControlButton, missing, missing, missing, true);
            XMLUnmapButton.Click += xButtonHandler;
            XMLUnmapButton.Caption = "Unmap Values";
            XMLUnmapButton.Visible = true;

            CellUnmapButton = (Office.CommandBarButton)Application.CommandBars["Cell"].Controls.Add(Office.MsoControlType.msoControlButton, missing, missing, missing, true);
            CellUnmapButton.Click += cButtonHandler;
            CellUnmapButton.Caption = "Clear Cell";
            CellUnmapButton.Visible = true;
        }

        private void unmapListClick(Office.CommandBarButton button, ref bool Cancel)
        {
            Excel.Range selected = (Excel.Range)this.Application.Selection;

            string cellXPath = selected.Cells.XPath.Value.ToLower();
            string cellContent = selected.Cells.Text.ToString();
            if (cellXPath.Contains("concept"))
            {
                if (!cellXPath.Contains("conceptual"))
                {
                    System.Collections.IEnumerator ir = selected.Hyperlinks.GetEnumerator();

                    string hAddy = "";
                    string hName = "";
                    if (ir != null)
                    {
                        while (ir.MoveNext())
                        {
                            Excel.Hyperlink hr = (Excel.Hyperlink)ir.Current;
                            hName = hr.Name;
                            hAddy = hr.SubAddress;
                        }
                        if (hName.Length > 0 && hAddy.Length > 0)
                        {
                            string listName = hAddy.Split('!')[0];

                            Excel.Worksheet list = (Excel.Worksheet)this.Application.Sheets[listName];
                            Excel.Range details = list.get_Range(hAddy, Type.Missing);

                            //Excel.Range c = (Excel.Range)list.Cells[2, 1];
                            Excel.Range c = details;
                            string hId = hName.Split('v')[0].Trim();
                            for (int i = details.Cells.Row; i < 10000; i++)
                            {
                                c = (Excel.Range)list.Cells[i, 1];
                                Excel.Range ce = (Excel.Range)list.Cells[i, 4];
                                string text = ce.Text.ToString();
                                if (text.Contains(hId))
                                    break;
                            }

                            list.Unprotect("dummy_password");
                            c.Clear();
                            c.Next.Clear();
                            c.Next.Next.Clear();
                            c.Next.Next.Next.Clear();
                            list.Protect("dummy_password", Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing);

                        }
                    }
                    else
                    {

                        Excel.Worksheet conceptList = (Excel.Worksheet)this.Application.Sheets["concept_list"];
                        string[] codes = selected.Text.ToString().Split(';');

                        selected.Cells.Clear();
                        selected.Cells.ClearContents();
                        selected.Cells.ClearFormats();
                        selected.Cells.ClearNotes();

                        if (conceptList != null)
                        {
                            conceptList.Unprotect("dummy_password");

                            //Use Excel built-in Find feature to search for matched row
                            foreach (String code in codes)
                            {
                                string c = code.Split(':')[0];
                                Excel.Range found = conceptList.Cells.Find(c, Type.Missing, Excel.XlFindLookIn.xlValues, Excel.XlLookAt.xlPart, Excel.XlSearchOrder.xlByRows, Excel.XlSearchDirection.xlNext, false, Type.Missing, Type.Missing);
                                if (found != null)
                                {
                                    int counter = Convert.ToInt16(found.Next.Next.Next.Next.Value2.ToString()) - 1;
                                    if (counter < 1)
                                    {
                                        found.EntireRow.Delete(Type.Missing); //Remove entire row
                                    }
                                    else
                                    {
                                        found.Next.Next.Next.Next.Value2 = counter;
                                    }
                                }
                            }

                            conceptList.Protect("dummy_password", Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing);
                        }
                    }
                }
                else
                    removeId(selected, "cd");
            }
            else if (cellXPath.Contains("cdeid"))
                removeId(selected, "cde");
            else if (cellXPath.Contains("vdid"))
                removeId(selected, "vd");
            else if (cellXPath.Contains("decid"))
                removeId(selected, "dec");
            else
                removeCommon(selected);

            selected.Cells.Clear();
            selected.Cells.ClearContents();
            //selected.Cells.ClearFormats();
            selected.Cells.ClearNotes();
        }

        private void removeCommon(Excel.Range selected)
        { }

        private void removeId(Excel.Range selected, string listType)
        {
            System.Collections.IEnumerator ir = selected.Hyperlinks.GetEnumerator();

            string hAddy = "";
            string hName = "";

            while (ir.MoveNext())
            {
                Excel.Hyperlink hr = (Excel.Hyperlink)ir.Current;
                hName = hr.Name;
                hAddy = hr.SubAddress;
            }
            //List name
            string listName = hAddy.Split('!')[0];

            //string selectedParent = ((Excel.Worksheet)selected.Parent).Name;
            //string selectedAddress = selected.get_Address(Type.Missing, Type.Missing, Excel.XlReferenceStyle.xlA1, Type.Missing, Type.Missing);
            //string selectedRangeString = ((Excel.Worksheet)selected.Parent).Name + "!" + selected.get_Address(Type.Missing, Type.Missing, Excel.XlReferenceStyle.xlA1, Type.Missing, Type.Missing);
            Excel.Worksheet list = (Excel.Worksheet)this.Application.Sheets[listName];
            Excel.Range details = list.get_Range(hAddy, Type.Missing);

            //Excel.Range c = (Excel.Range)list.Cells[2, 1];
            Excel.Range c = details;
            string hId = hName.Split('v')[0];
            for (int i = details.Cells.Row; i < 10000; i++)
            {
                c = (Excel.Range)list.Cells[i, 1];
                string text = c.Text.ToString();
                if (text.Contains(hId))
                    break;
            }

            list.Unprotect("dummy_password");
            c.Clear();
            c.Next.Clear();
            c.Next.Next.Clear();
            c.Next.Next.Next.Clear();
            c.Next.Next.Next.Next.Clear();
            list.Protect("dummy_password", Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing);

        }

        private void unmapXMLClick(Office.CommandBarButton button, ref bool Cancel)
        {
            Excel.Range selected = (Excel.Range)this.Application.Selection;

            System.Collections.IEnumerator ir = selected.Hyperlinks.GetEnumerator();

            string hAddy = "";
            string hName = "";
            if (ir != null)
            {
                while (ir.MoveNext())
                {
                    Excel.Hyperlink hr = (Excel.Hyperlink)ir.Current;
                    hName = hr.Name;
                    hAddy = hr.SubAddress;
                }
                if (hName.Length > 0 && hAddy.Length > 0)
                {
                    string listName = hAddy.Split('!')[0];

                    Excel.Worksheet list = (Excel.Worksheet)this.Application.Sheets[listName];
                    Excel.Range details = list.get_Range(hAddy, Type.Missing);

                    //Excel.Range c = (Excel.Range)list.Cells[2, 1];
                    Excel.Range c = details;
                    string hId = hName.Split('v')[0];
                    for (int i = details.Cells.Row; i < 10000; i++)
                    {
                        c = (Excel.Range)list.Cells[i, 1];
                        string text = c.Text.ToString();
                        if (text.Contains(hId))
                            break;
                    }

                    list.Unprotect("dummy_password");
                    c.Clear();
                    c.Next.Clear();
                    c.Next.Next.Clear();
                    c.Next.Next.Next.Clear();
                    list.Protect("dummy_password", Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing);

                }
            }

            selected.Cells.Clear();
            selected.Cells.ClearContents();
            selected.Cells.ClearFormats();
            selected.Cells.ClearNotes();

            //string selectedRangeString = ((Excel.Worksheet)selected.Parent).Name + "!" + selected.get_Address(Type.Missing, Type.Missing, Excel.XlReferenceStyle.xlA1, Type.Missing, Type.Missing);
            //Excel.Worksheet cdeList = (Excel.Worksheet)this.Application.Sheets["cde_list"];

            //Excel.Range c = (Excel.Range)cdeList.Cells[2, 1];
            //for (int i = 2; i < 10000; i++)
            //{
            //    c = (Excel.Range)cdeList.Cells[i, 1];
            //    string text = c.Text.ToString();
            //    if (text.Contains(selectedRangeString))
            //        break;
            //}

            //cdeList.Unprotect("dummy_password");
            //c.Clear();
            //c.Next.Clear();
            //c.Next.Next.Clear();
            //c.Next.Next.Next.Clear();
            //cdeList.Protect("dummy_password", Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing);

        }

        private void unmapHeaderClick(Office.CommandBarButton button, ref bool Cancel)
        {
            Excel.Range selected = (Excel.Range)this.Application.Selection;
            System.Collections.IEnumerator ir = selected.Hyperlinks.GetEnumerator();

            bool concept = false;
            string hAddy = "";
            string hName = "";
            if (ir != null)
            {
                while (ir.MoveNext())
                {
                    Excel.Hyperlink hr = (Excel.Hyperlink)ir.Current;
                    hName = hr.Name;
                    hAddy = hr.SubAddress;
                }
                if (hName.Length > 0 && hAddy.Length > 0)
                {
                    string listName = hAddy.Split('!')[0];

                    Excel.Worksheet list = (Excel.Worksheet)this.Application.Sheets[listName];
                    Excel.Range details = list.get_Range(hAddy, Type.Missing);

                    //Excel.Range c = (Excel.Range)list.Cells[2, 1];
                    Excel.Range c = details;
                    string hId = hName.Split('v')[0];
                    for (int i = details.Cells.Row; i < 10000; i++)
                    {
                        c = (Excel.Range)list.Cells[i, 1];
                        string text = c.Text.ToString();
                        if (text.Contains(hId))
                            break;
                    }

                    list.Unprotect("dummy_password");
                    c.Clear();
                    c.Next.Clear();
                    c.Next.Next.Clear();
                    c.Next.Next.Next.Clear();
                    list.Protect("dummy_password", Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing);
                }
                else
                    concept = true;
            }
            else
                concept = true;
            if (concept)
            {
                string[] codes = selected.Text.ToString().Split(';');
                selected.Cells.Clear();
                selected.Cells.ClearContents();
                selected.Cells.ClearFormats();
                selected.Cells.ClearNotes();

                Excel.Worksheet conceptList = (Excel.Worksheet)this.Application.Sheets["concept_list"];

                if (conceptList != null)
                {
                    conceptList.Unprotect("dummy_password");

                    //Use Excel built-in Find feature to search for matched row
                    foreach (String code in codes)
                    {
                        string c = code.Split(':')[0];
                        Excel.Range found = conceptList.Cells.Find(c, Type.Missing, Excel.XlFindLookIn.xlValues, Excel.XlLookAt.xlPart, Excel.XlSearchOrder.xlByRows, Excel.XlSearchDirection.xlNext, false, Type.Missing, Type.Missing);
                        if (found != null)
                        {
                            int counter = Convert.ToInt16(found.Next.Next.Next.Next.Value2.ToString()) - 1;
                            if (counter < 1)
                            {
                                found.EntireRow.Delete(Type.Missing); //Remove entire row
                            }
                            else
                            {
                                found.Next.Next.Next.Next.Value2 = counter;
                            }
                        }
                    }

                    conceptList.Protect("dummy_password", Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing, Type.Missing);
                }
            }

            selected.Cells.Clear();
            selected.Cells.ClearContents();
            selected.Cells.ClearFormats();
            selected.Cells.ClearNotes();
        }

        public void AddQueryServiceTaskPane()
        {
            if (myCustomTaskPane == null)
            {
                queryServiceControl = new ExcelQueryServiceControl();
                myCustomTaskPane = this.CustomTaskPanes.Add(queryServiceControl, "Query Service Control");
                myCustomTaskPane.DockPosition = Microsoft.Office.Core.MsoCTPDockPosition.msoCTPDockPositionRight;
                myCustomTaskPane.Width = 300;
                myCustomTaskPane.Visible = true;
            }
            else if (myCustomTaskPane.Visible == false)
            {
                myCustomTaskPane.Visible = true;
            }
        }

        public void RemoveQueryServiceTaskPane()
        {
            try
            {
                if (myCustomTaskPane != null && this.CustomTaskPanes.Contains(myCustomTaskPane))
                {
                    this.CustomTaskPanes.Remove(myCustomTaskPane);
                }
            }
            catch (Exception) { }
            myCustomTaskPane = null;
        }

        protected override Microsoft.Office.Core.IRibbonExtensibility CreateRibbonExtensibilityObject()
        {
            return new ExcelQueryServiceRibbon();
        }

        #region VSTO generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InternalStartup()
        {
            this.Startup += new System.EventHandler(ThisAddIn_Startup);
            this.Shutdown += new System.EventHandler(ThisAddIn_Shutdown);
        }

        #endregion
    }
}
