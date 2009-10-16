using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Xml.Linq;
using InfoPath = Microsoft.Office.Interop.InfoPath;
using Office = Microsoft.Office.Core;
using OfficeTools = Microsoft.Office.Tools;

namespace InfoPathQueryServiceAddIn
{
    public partial class ThisAddIn
    {
        private InfoPath.ApplicationEvents appevents;
        private InfoPath._Application3 app3;

        public bool showTaskPane = false;

        private Office.CommandBar mainMenuBar = null;
        private Object helpMenuIndex = null;
        private Office.CommandBarPopup vstoAddInMenu = null;
        private Office.CommandBarButton queryFormMenuItem = null;
        private Office.CommandBarButton cdeFormMenuItem = null;
        private Microsoft.Office.Tools.CustomTaskPane ctpTaskPane = null;
        private OfficeTools.CustomTaskPane ctp = null;
        
        private void ThisAddIn_Startup(object sender, System.EventArgs e)
        {
            app3 = (InfoPath._Application3)this.Application;
            appevents = (InfoPath.ApplicationEvents)app3.Events;

            appevents.NewXDocument += new InfoPath._ApplicationEvents_NewXDocumentEventHandler(appevents_NewXDocument);
            appevents.XDocumentOpen += new InfoPath._ApplicationEvents_XDocumentOpenEventHandler(appevents_XDocumentOpen);
            appevents.XDocumentChange += new InfoPath._ApplicationEvents_XDocumentChangeEventHandler(appevents_DocumentChange);

            //appevents.WindowActivate += new InfoPath._ApplicationEvents_WindowActivateEventHandler(InfoPahtApplicationEvents_WindowActivate);
            //appevents.WindowDeactivate += new InfoPath._ApplicationEvents_WindowDeactivateEventHandler(InfoPahtApplicationEvents_WindowDeactivate);

            AddMenu();
            //AddAllQueryServiceTaskPanes();
        }

        private void ThisAddIn_Shutdown(object sender, System.EventArgs e)
        {
            showTaskPane = false;
        }

        private void appevents_NewXDocument(InfoPath._XDocument pDocument)
        {
            AddMenu();
            if (showTaskPane == true)
            {
                AddQueryServiceTaskPane(this.Application.ActiveWindow);
            }
        }

        private void appevents_XDocumentOpen(InfoPath._XDocument pDocument)
        {
            AddMenu();
            RemoveOrphanedTaskPanes();
            if (showTaskPane == true)
            {
                AddQueryServiceTaskPane(this.Application.ActiveWindow);
            }
        }

        private void appevents_DocumentChange()
        {
            RemoveOrphanedTaskPanes();
        }

        public void AddAllQueryServiceTaskPanes()
        {
            foreach (InfoPath.Window _window in this.Application.Windows)
            {
                AddQueryServiceTaskPane(_window);
            }
        }

        public void RemoveAllQueryServiceTaskPanes()
        {
            for (int i = this.CustomTaskPanes.Count; i > 0; i--)
            {
                ctp = this.CustomTaskPanes[i - 1];
                if (ctp.Title == "Query Service Control")
                {
                    //this.CustomTaskPanes.RemoveAt(i - 1);
                    //ctpTaskPane = null;
                    ctpTaskPane.Visible = false;
                }
            }
        }

        public void AddQueryServiceTaskPane(InfoPath.Window _window)
        {
            if (ctpTaskPane == null)
            {
                ctpTaskPane = this.CustomTaskPanes.Add(new InfoPathQueryServiceControl(), "Query Service Control", _window);
            }
            ctpTaskPane.Visible = true;
        }

        private void RemoveOrphanedTaskPanes()
        {
            for (int i = this.CustomTaskPanes.Count; i > 0; i--)
            {
                ctp = this.CustomTaskPanes[i - 1];
                if (ctp.Window == null)
                {
                    this.CustomTaskPanes.Remove(ctp);
                }
            }
        }

        public void AddMenu()
        {
            try
            {
                mainMenuBar = ((Office.CommandBars)this.Application.ActiveWindow.CommandBars).ActiveMenuBar;
            }
            catch (Exception e)
            {
                string message = e.Message;
                return;
            }

            if (vstoAddInMenu != null)
            {
                vstoAddInMenu.Visible = true;
                return;
            }

            try
            {
                helpMenuIndex = mainMenuBar.Controls["Help"].Index;
            }
            catch (Exception)
            {
                helpMenuIndex = mainMenuBar.Controls.Count;
            }
            vstoAddInMenu = (Office.CommandBarPopup)mainMenuBar.Controls.Add(Office.MsoControlType.msoControlPopup, Type.Missing, Type.Missing, helpMenuIndex, true);
            vstoAddInMenu.Caption = "CancerGrid";
            vstoAddInMenu.Visible = true;

            queryFormMenuItem = (Office.CommandBarButton)vstoAddInMenu.Controls.Add(Office.MsoControlType.msoControlButton, Type.Missing, Type.Missing, 1, 1);
            queryFormMenuItem.Style = Office.MsoButtonStyle.msoButtonCaption;
            queryFormMenuItem.Caption = "Query Service Control";
            queryFormMenuItem.Click += new Office._CommandBarButtonEvents_ClickEventHandler(queryFormMenuItem_Click);

            cdeFormMenuItem = (Office.CommandBarButton)vstoAddInMenu.Controls.Add(Office.MsoControlType.msoControlButton, Type.Missing, Type.Missing, 1, 1);
            cdeFormMenuItem.Style = Office.MsoButtonStyle.msoButtonCaption;
            cdeFormMenuItem.Caption = "Create new Data Element";
            cdeFormMenuItem.Click += new Office._CommandBarButtonEvents_ClickEventHandler(cdeFormMenuItem_Click);
            //menuAdded = true;
        }

        void queryFormMenuItem_Click(Office.CommandBarButton Ctrl, ref bool CancelDefault)
        {
            if (ctpTaskPane == null || ctpTaskPane.Visible == false)
            {
                // Add the query task pane to all open documents
                AddAllQueryServiceTaskPanes();
            }
            else
            {
                // Remove the query task pane from all open documents
                RemoveAllQueryServiceTaskPanes();
            }
        }

        void cdeFormMenuItem_Click(Office.CommandBarButton Ctrl, ref bool CancelDefault)
        {
            DataElementCreationForm dataElementCrerationForm = new DataElementCreationForm();
            dataElementCrerationForm.Show();
        }

        void InfoPahtApplicationEvents_WindowActivate(InfoPath._XDocument pDocument, InfoPath.Window pWindow)
        {
            mainMenuBar = ((Office.CommandBars)this.Application.ActiveWindow.CommandBars).ActiveMenuBar;
            if (vstoAddInMenu != null)
            {
                vstoAddInMenu.Visible = true;
            }
            else
            {
                AddMenu();
            }

            foreach (Microsoft.Office.Tools.CustomTaskPane taskpane in this.CustomTaskPanes)
            {
                if (taskpane.Title == "Query Service Control")
                {
                    taskpane.Visible = true;
                    return;
                }
            }
            AddQueryServiceTaskPane(this.Application.ActiveWindow);
        }

        void InfoPahtApplicationEvents_WindowDeactivate(InfoPath._XDocument pDocument, InfoPath.Window pWindow)
        {

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
