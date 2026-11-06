Imports System.Threading.Tasks
Imports System.Windows.Media.Effects
Imports System.Windows.Media.Imaging

Public Interface IGlassPanel

    Sub GlassRefreshBackdrop()
End Interface

Friend Class GlassDialogPanel
    Implements IGlassPanel
    Public ReadOnly Border As Border
    Public ReadOnly Backdrop As Shapes.Rectangle
    Public Sub New(Border As Border, Backdrop As Shapes.Rectangle)
        Me.Border = Border
        Me.Backdrop = Backdrop
    End Sub
    Public Sub GlassRefreshBackdrop() Implements IGlassPanel.GlassRefreshBackdrop
        If Border Is Nothing OrElse Backdrop Is Nothing Then Return
        Backdrop.Fill = ModGlass.GlassSurfaceBrush(Border, True)
    End Sub
End Class

Public Module ModGlass

    Friend ReadOnly GlassHostTag As New Object

#Region "参数"

    Private Const CornerDefault As Double = 16
    Private Const UiBlurDefault As Double = 34

    Private Const MaxSurfacePixels As Double = 4200000

    Friend Function SnapshotScale(Element As FrameworkElement, Width As Double, Height As Double) As Double

        Dim Scale As Double = 0
        Try
            Scale = VisualTreeHelper.GetDpi(Element).DpiScaleX
        Catch
        End Try

        Try
            Scale = Math.Max(Scale, DPI / 96.0)
        Catch
        End Try
        If Double.IsNaN(Scale) OrElse Scale < 1.0 Then Scale = 1.0
        Scale = Math.Min(Scale, 3.0)

        Dim Pixels As Double = Math.Max(1, Width) * Math.Max(1, Height) * Scale * Scale
        If Pixels > MaxSurfacePixels Then Scale *= Math.Sqrt(MaxSurfacePixels / Pixels)
        Return Math.Max(1.0, Scale)
    End Function

    Public ReadOnly Property GlassCornerRadius As Double
        Get
            Dim Value As Integer = CornerDefault
            Try
                Value = Settings.Get(Of Integer)("UiGlassCorner")
            Catch
            End Try
            Return Math.Max(0, Math.Min(28, Value))
        End Get
    End Property

    Public ReadOnly Property GlassUiBlur As Double
        Get
            Dim Value As Integer = UiBlurDefault
            Try
                Value = Settings.Get(Of Integer)("UiUiBlur")
            Catch
            End Try
            Return Math.Max(0, Math.Min(60, Value))
        End Get
    End Property

    Public ReadOnly Property GlassCardOpacity As Integer
        Get
            Try
                Return Math.Max(0, Math.Min(100, Settings.Get(Of Integer)("UiGlassCardOpacity")))
            Catch
                Return 100
            End Try
        End Get
    End Property

#End Region

#Region "笔刷（冻结复用，全模块共享）"

    Private _Frost As Brush = Nothing
    Private _FrostKey As Integer = -1

    Public Function GlassCardTint() As Brush

        Dim Factor As Integer = Math.Max(0, Math.Min(100, GlassCardOpacity))
        If Factor <= 0 Then Return Nothing
        If _Frost IsNot Nothing AndAlso _FrostKey = Factor Then Return _Frost
        Dim Brush As New LinearGradientBrush With {.StartPoint = New Point(0, 0), .EndPoint = New Point(0.35, 1)}
        Brush.GradientStops.Add(New GradientStop(Color.FromArgb(CByte(22 * Factor \ 100), 255, 255, 255), 0))
        Brush.GradientStops.Add(New GradientStop(Color.FromArgb(CByte(18 * Factor \ 100), 255, 255, 255), 0.16))
        Brush.GradientStops.Add(New GradientStop(Color.FromArgb(CByte(26 * Factor \ 100), 253, 254, 255), 0.56))
        Brush.GradientStops.Add(New GradientStop(Color.FromArgb(CByte(34 * Factor \ 100), 251, 252, 255), 1))
        Brush.Freeze()
        _Frost = Brush
        _FrostKey = Factor
        Return Brush
    End Function

    Private _DialogTint As Brush = Nothing

    Public Function GlassDialogTint() As Brush
        If _DialogTint IsNot Nothing Then Return _DialogTint
        Dim Brush As New LinearGradientBrush(Color.FromArgb(150, 255, 255, 255), Color.FromArgb(120, 246, 250, 255), New Point(0, 0), New Point(0.3, 1))
        Brush.Freeze()
        _DialogTint = Brush
        Return Brush
    End Function

    Private _SolidTint As Brush = Nothing

    Public Function GlassSolidTint() As Brush
        If _SolidTint IsNot Nothing Then Return _SolidTint
        Dim Brush As New SolidColorBrush(Color.FromArgb(242, 255, 255, 255))
        Brush.Freeze()
        _SolidTint = Brush
        Return Brush
    End Function

    Private _VideoTint As Brush = Nothing
    Private _VideoTintKey As String = ""

    Public Function GlassVideoTint() As Brush

        Dim Blur As Double = Math.Max(0, Math.Min(60, GlassUiBlur))
        Dim Factor As Integer = Math.Max(0, Math.Min(100, GlassCardOpacity))
        Dim Key As String = $"{CInt(Blur)}|{Factor}"
        If _VideoTintKey = Key AndAlso (_VideoTint IsNot Nothing OrElse Blur <= 0.5 OrElse Factor <= 0) Then Return _VideoTint
        _VideoTintKey = Key
        If Blur <= 0.5 OrElse Factor <= 0 Then
            _VideoTint = Nothing
            Return Nothing
        End If

        Dim K As Double = (Blur - 1) / 59.0
        Dim Scale As Double = Factor / 100.0
        Dim Brush As New LinearGradientBrush With {.StartPoint = New Point(0, 0), .EndPoint = New Point(0.4, 1)}
        Brush.GradientStops.Add(New GradientStop(Color.FromArgb(CByte(Math.Round((5 + 21 * K) * Scale)), 255, 255, 255), 0))
        Brush.GradientStops.Add(New GradientStop(Color.FromArgb(CByte(Math.Round((7 + 27 * K) * Scale)), 250, 251, 253), 0.5))
        Brush.GradientStops.Add(New GradientStop(Color.FromArgb(CByte(Math.Round((10 + 34 * K) * Scale)), 244, 247, 252), 1))
        Brush.Freeze()
        _VideoTint = Brush
        Return Brush
    End Function

    Public ReadOnly Property IsVideoBackdrop As Boolean
        Get
            Try
                Return FrmMain IsNot Nothing AndAlso FrmMain.IsVideoBackgroundActive
            Catch
                Return False
            End Try
        End Get
    End Property

    Private _Edge As Brush = Nothing

    Public Function GlassEdgeBrush() As Brush
        If _Edge IsNot Nothing Then Return _Edge
        Dim Brush As New SolidColorBrush(Color.FromArgb(76, 255, 255, 255))
        Brush.Freeze()
        _Edge = Brush
        Return Brush
    End Function

    Private _CardEdge As Brush = Nothing

    Public Function GlassCardEdgeBrush() As Brush
        If _CardEdge IsNot Nothing Then Return _CardEdge
        Dim Brush As New SolidColorBrush(Color.FromArgb(52, 255, 255, 255))
        Brush.Freeze()
        _CardEdge = Brush
        Return Brush
    End Function

#End Region

#Region "离屏模糊"

    Private Function SnapshotVisual(Element As FrameworkElement, Width As Double, Height As Double, Scale As Double, PixelWidth As Integer, PixelHeight As Integer) As RenderTargetBitmap
        Dim Shot As New RenderTargetBitmap(Math.Max(1, PixelWidth), Math.Max(1, PixelHeight), 96 * Scale, 96 * Scale, PixelFormats.Pbgra32)
        Dim Wrap As New DrawingVisual()
        Using DC As DrawingContext = Wrap.RenderOpen()
            Dim VB As New VisualBrush(Element) With {
                .Stretch = Stretch.None,
                .AlignmentX = AlignmentX.Left,
                .AlignmentY = AlignmentY.Top,
                .TileMode = TileMode.None}
            DC.DrawRectangle(VB, Nothing, New Rect(0, 0, Math.Max(1, Width), Math.Max(1, Height)))
        End Using
        Shot.Render(Wrap)
        Shot.Freeze()
        Return Shot
    End Function

    Private Function Blur(Source As BitmapSource, Radius As Double, PixelWidth As Integer, PixelHeight As Integer) As BitmapSource
        If Radius <= 0.35 Then Return Source
        Dim W As Integer = Math.Max(1, PixelWidth)
        Dim H As Integer = Math.Max(1, PixelHeight)
        Try
            Dim Stride As Integer = W * 4
            Dim Raw(Stride * H - 1) As Byte
            Dim Flat As BitmapSource = Source
            If Flat.Format <> PixelFormats.Pbgra32 Then Flat = New FormatConvertedBitmap(Source, PixelFormats.Pbgra32, Nothing, 0)
            Flat.CopyPixels(Raw, Stride, 0)

            Dim BoxRadius As Integer = Math.Max(1, CInt(Math.Round(Radius * 0.5)))
            Dim Plane(W * H - 1) As Byte
            Dim Temp(W * H - 1) As Byte
            Dim Offset As Integer
            For Channel As Integer = 0 To 3
                Offset = Channel
                For P As Integer = 0 To W * H - 1
                    Plane(P) = Raw(Offset)
                    Offset += 4
                Next
                For Pass As Integer = 1 To 3
                    BlurPlane(Plane, Temp, W, H, BoxRadius)
                Next
                Offset = Channel
                For P As Integer = 0 To W * H - 1
                    Raw(Offset) = Plane(P)
                    Offset += 4
                Next
            Next
            Dim Result As BitmapSource = BitmapSource.Create(W, H, 96, 96, PixelFormats.Pbgra32, Nothing, Raw, Stride)
            Result.Freeze()
            Return Result
        Catch ex As Exception
            Dim Inner As Exception = ex
            Do While Inner.InnerException IsNot Nothing
                Inner = Inner.InnerException
            Loop
            Logger.Info($"[Glass] 盒式模糊失败，回退到 BlurEffect：{Inner.GetType().Name} {Inner.Message}")
            Return BlurByEffect(Source, Radius, W, H)
        End Try
    End Function

    Private Sub BlurPlane(Plane() As Byte, Temp() As Byte, W As Integer, H As Integer, R As Integer)
        Dim Count As Integer = R * 2 + 1
        Dim LastW As Integer = W - 1
        Dim LastH As Integer = H - 1

        Parallel.For(0, H,
            Sub(Y As Integer)
                Dim Base As Integer = Y * W
                Dim Sum As Integer = 0
                For K As Integer = -R To R
                    Sum += Plane(Base + Math.Min(LastW, Math.Max(0, K)))
                Next
                For X As Integer = 0 To LastW
                    Temp(Base + X) = CByte(Sum \ Count)
                    Sum += CInt(Plane(Base + Math.Min(LastW, X + R + 1))) - CInt(Plane(Base + Math.Max(0, X - R)))
                Next
            End Sub)
        Parallel.For(0, W,
            Sub(X As Integer)
                Dim Sum As Integer = 0
                For K As Integer = -R To R
                    Sum += Temp(Math.Min(LastH, Math.Max(0, K)) * W + X)
                Next
                For Y As Integer = 0 To LastH
                    Plane(Y * W + X) = CByte(Sum \ Count)
                    Sum += CInt(Temp(Math.Min(LastH, Y + R + 1) * W + X)) - CInt(Temp(Math.Max(0, Y - R) * W + X))
                Next
            End Sub)
    End Sub

    Private Function BlurByEffect(Source As BitmapSource, Radius As Double, W As Integer, H As Integer) As BitmapSource
        Dim Draw As New DrawingVisual()
        Draw.Effect = New BlurEffect With {.Radius = Radius, .KernelType = KernelType.Gaussian, .RenderingBias = RenderingBias.Performance}
        Using DC As DrawingContext = Draw.RenderOpen()
            DC.DrawImage(Source, New Rect(0, 0, W, H))
        End Using
        Dim Out As New RenderTargetBitmap(Math.Max(1, W), Math.Max(1, H), 96, 96, PixelFormats.Pbgra32)
        Out.Render(Draw)
        Out.Freeze()
        Return Out
    End Function

#End Region

#Region "壁纸快照（卡片 / 侧栏）"

    Public GlassReady As Boolean = False

    Private _Wall As BitmapSource = Nothing
    Private _WallKey As String = ""

    Public Sub GlassInvalidate()
        _Wall = Nothing
        _WallKey = ""
        _Snapshot = Nothing
        _SnapshotGeneration = -1
        _Revision += 1
    End Sub

    Private _Revision As Integer = 0

    Public ReadOnly Property SurfaceRevision As Integer
        Get
            Return _Revision
        End Get
    End Property

    Private Function WallBitmap() As BitmapSource
        Dim Img As Panel = Nothing
        Dim RawOpacity As Double = 1
        Dim RawEffect As Effects.Effect = Nothing
        Dim Cleaned As Boolean = False
        Try
            If Not GlassReady Then Return Nothing
            If FrmMain Is Nothing OrElse FrmMain.ImgBack Is Nothing Then Return Nothing
            Img = FrmMain.ImgBack
            If Img.Visibility <> Visibility.Visible OrElse Img.ActualWidth < 32 OrElse Img.ActualHeight < 32 Then Return Nothing

            If Img.Opacity <= 0.01 Then Return Nothing

            Dim Radius As Double = GlassUiBlur
            If Radius <= 0.5 Then Return Nothing

            Dim SourceHash As Integer = 0
            Try
                Dim WallBrush = TryCast(Img.Background, ImageBrush)
                If WallBrush IsNot Nothing AndAlso WallBrush.ImageSource IsNot Nothing Then SourceHash = WallBrush.ImageSource.GetHashCode()
            Catch
            End Try
            Dim Key As String = $"{CInt(Img.ActualWidth) \ 64}x{CInt(Img.ActualHeight) \ 64}|{CInt(Radius)}|{CInt(Img.Opacity * 100)}|{SourceHash}|{If(Img.Effect Is Nothing, 0, 1)}|{CInt(Img.Margin.Left)}|{If(Img.Background Is Nothing, 0, Img.Background.GetHashCode())}"
            If _Wall IsNot Nothing AndAlso _WallKey = Key Then Return _Wall

            RawOpacity = Img.Opacity
            RawEffect = Img.Effect
            If RawOpacity < 0.999 OrElse RawEffect IsNot Nothing Then
                Img.Opacity = 1
                Img.Effect = Nothing
                Cleaned = True
            End If
            Dim Scale As Double = SnapshotScale(Img, Img.ActualWidth, Img.ActualHeight)
            Dim Wi As Integer = Math.Max(1, CInt(Math.Ceiling(Img.ActualWidth * Scale)))
            Dim Hi As Integer = Math.Max(1, CInt(Math.Ceiling(Img.ActualHeight * Scale)))
            Dim Watch As Stopwatch = Stopwatch.StartNew()
            Dim Shot As RenderTargetBitmap = SnapshotVisual(Img, Img.ActualWidth, Img.ActualHeight, Scale, Wi, Hi)
            Dim Result As BitmapSource = Blur(Shot, Radius * Scale, Wi, Hi)
            Watch.Stop()
            _Wall = Result
            _WallKey = Key
            Logger.Info($"[Glass] 重建壁纸底图：{Wi}x{Hi}（{Scale:F2}x，原 {CInt(Img.ActualWidth)}x{CInt(Img.ActualHeight)} DIP），半径 {Radius}，透明度 {RawOpacity}，画笔 {Img.Background.GetHashCode()}，图源 {SourceHash}，耗时 {Watch.ElapsedMilliseconds} ms")
            Return Result
        Catch ex As Exception
            Logger.Info($"[Glass] 壁纸快照失败：{ex.Message}")
            Return Nothing
        Finally

            If Cleaned AndAlso Img IsNot Nothing Then
                Try
                    Img.Opacity = RawOpacity
                    Img.Effect = RawEffect
                Catch
                End Try
            End If
        End Try
    End Function

#End Region

#Region "界面快照（弹窗，真正的 UI 模糊）"

    Private _Snapshot As BitmapSource = Nothing
    Private _SnapshotGeneration As Integer = -1
    Private _Generation As Integer = 0

    Public Sub GlassInvalidateSnapshot()
        _Generation += 1
    End Sub

    Private Function SnapshotBitmap() As BitmapSource
        Try
            If Not GlassReady Then Return Nothing
            If GlassUiBlur <= 0.5 Then Return Nothing
            If FrmMain Is Nothing Then Return Nothing
            Dim PanForm = FrmMain.PanForm
            If PanForm Is Nothing Then Return Nothing
            Dim W As Double = PanForm.ActualWidth, H As Double = PanForm.ActualHeight
            If W < 32 OrElse H < 32 Then Return Nothing
            If _Snapshot IsNot Nothing AndAlso _SnapshotGeneration = _Generation Then Return _Snapshot
            Dim Scale As Double = SnapshotScale(PanForm, W, H)
            Dim Wi As Integer = Math.Max(1, CInt(Math.Ceiling(W * Scale)))
            Dim Hi As Integer = Math.Max(1, CInt(Math.Ceiling(H * Scale)))
            Dim Watch As Stopwatch = Stopwatch.StartNew()
            Dim Shot As RenderTargetBitmap = SnapshotVisual(PanForm, W, H, Scale, Wi, Hi)
            Dim Result As BitmapSource = Blur(Shot, GlassUiBlur * Scale, Wi, Hi)
            Watch.Stop()
            _Snapshot = Result
            _SnapshotGeneration = _Generation
            Logger.Info($"[Glass] 重建界面快照：{Wi}x{Hi}（{Scale:F2}x），半径 {GlassUiBlur}，耗时 {Watch.ElapsedMilliseconds} ms")
            Return Result
        Catch ex As Exception
            Logger.Info($"[Glass] 界面快照失败：{ex.Message}")
            Return Nothing
        End Try
    End Function

#End Region

#Region "对齐"

    Public Function CreateSurfaceBrush() As ImageBrush
        Return New ImageBrush With {
            .Stretch = Stretch.Fill,
            .ViewboxUnits = BrushMappingMode.Absolute,
            .ViewportUnits = BrushMappingMode.Absolute,
            .AlignmentX = AlignmentX.Left,
            .AlignmentY = AlignmentY.Top,
            .TileMode = TileMode.None}
    End Function

    Public GlassDiagLog As Boolean = False
    Private _DiagCount As Integer = 0

    Public Function AlignSurfaceBrush(Brush As ImageBrush, Element As FrameworkElement, Optional UseSnapshot As Boolean = False, Optional RequireLaidOut As Boolean = False) As Boolean
        Try
            If Brush Is Nothing OrElse Element Is Nothing OrElse FrmMain Is Nothing Then Return False
            Dim Reference As FrameworkElement
            Dim Bitmap As BitmapSource
            If UseSnapshot Then
                Reference = FrmMain.PanForm
                Bitmap = SnapshotBitmap()
            Else
                Reference = FrmMain.ImgBack
                Bitmap = WallBitmapCached()
            End If
            If Bitmap Is Nothing OrElse Reference Is Nothing Then Return False

            If RequireLaidOut AndAlso (Element.ActualWidth < 1 OrElse Element.ActualHeight < 1) Then Return False
            Dim Offset As Point
            Try
                Offset = Element.TranslatePoint(New Point(0, 0), Reference)
            Catch
                Return False
            End Try
            If Double.IsNaN(Offset.X) OrElse Double.IsNaN(Offset.Y) Then Return False
            If Not Object.ReferenceEquals(Brush.ImageSource, Bitmap) Then
                Brush.ImageSource = Bitmap
                Brush.Viewbox = New Rect(0, 0, Bitmap.PixelWidth, Bitmap.PixelHeight)
            End If

            Dim BiasX As Double = 0, BiasY As Double = 0
            Dim Bd As Border = TryCast(Element, Border)
            If Bd IsNot Nothing Then
                BiasX = Bd.BorderThickness.Left
                BiasY = Bd.BorderThickness.Top
            End If
            Dim NewViewport As New Rect(-Offset.X - BiasX, -Offset.Y - BiasY, Math.Max(1, Reference.ActualWidth), Math.Max(1, Reference.ActualHeight))
            If Brush.Viewport <> NewViewport Then Brush.Viewport = NewViewport
            If GlassDiagLog AndAlso _DiagCount < 200 Then
                _DiagCount += 1
                Try
                    Logger.Info($"[GlassDiag] {Element.GetType().Name}#{_DiagCount} elem={Element.ActualWidth:F1}x{Element.ActualHeight:F1} off=({Offset.X:F1},{Offset.Y:F1}) bmp={Bitmap.PixelWidth}x{Bitmap.PixelHeight} vp=({NewViewport.X:F1},{NewViewport.Y:F1},{NewViewport.Width:F1},{NewViewport.Height:F1})")
                Catch
                End Try
            End If
            Return True
        Catch
            Return False
        End Try
    End Function

    Public Function GlassSurfaceBrush(Element As FrameworkElement, Optional UseSnapshot As Boolean = False) As Brush
        Try
            Dim Brush As ImageBrush = CreateSurfaceBrush()
            If Not AlignSurfaceBrush(Brush, Element, UseSnapshot) Then Return Nothing
            Brush.Freeze()
            Return Brush
        Catch
            Return Nothing
        End Try
    End Function

    Public Function WallBitmapCached() As BitmapSource
        If _Wall IsNot Nothing Then Return _Wall
        Return WallBitmap()
    End Function

    Public Sub GlassRefreshFill(Element As Shapes.Shape)
        If Element Is Nothing Then Return
        Try
            Dim Brush As Brush = GlassSurfaceBrush(Element, False)
            If Brush IsNot Nothing Then
                Element.Fill = Brush
            ElseIf IsVideoBackdrop Then

                Element.Fill = GlassVideoTint()
            ElseIf GlassReady Then

                Element.Fill = Nothing
            End If
        Catch
        End Try
    End Sub

#End Region

#Region "壁纸变化监视（自愈）"

    Private _WatchTimer As Windows.Threading.DispatcherTimer = Nothing
    Private _WatchSignature As String = ""
    Private _OnWallpaperChanged As Action = Nothing

    Public Sub GlassWatchWallpaper(OnChanged As Action)
        _OnWallpaperChanged = OnChanged
        If _WatchTimer IsNot Nothing Then Return
        _WatchTimer = New Windows.Threading.DispatcherTimer With {.Interval = TimeSpan.FromMilliseconds(300)}
        AddHandler _WatchTimer.Tick, AddressOf WatchWallpaper
        _WatchTimer.Start()
    End Sub

    Private Function WallpaperSignature() As String
        Try
            If FrmMain Is Nothing OrElse FrmMain.ImgBack Is Nothing Then Return ""
            Dim Img = FrmMain.ImgBack
            Dim Brush = TryCast(Img.Background, ImageBrush)
            Dim SourceHash As Integer = 0
            If Brush IsNot Nothing AndAlso Brush.ImageSource IsNot Nothing Then SourceHash = Brush.ImageSource.GetHashCode()
            Return $"{Img.Visibility}|{CInt(Img.Opacity * 100)}|{CInt(Img.ActualWidth) \ 64}x{CInt(Img.ActualHeight) \ 64}|{SourceHash}|{If(Brush Is Nothing, 0, Brush.GetHashCode())}|{CInt(GlassUiBlur)}"
        Catch
            Return ""
        End Try
    End Function

    Private Sub WatchWallpaper(sender As Object, e As EventArgs)
        Try
            Dim Signature As String = WallpaperSignature()
            If Signature = _WatchSignature Then Return
            _WatchSignature = Signature
            GlassInvalidate()
            If _OnWallpaperChanged IsNot Nothing Then _OnWallpaperChanged()
        Catch ex As Exception
            Logger.Info($"[Glass] 壁纸变化监视失败：{ex.Message}")
        End Try
    End Sub

#End Region

#Region "对齐去抖队列"

    Private ReadOnly _Pending As New List(Of WeakReference)
    Private _FlushTimer As Windows.Threading.DispatcherTimer = Nothing

    Private _SuspendCount As Integer = 0

    Public Sub SuspendRefresh()
        _SuspendCount += 1
    End Sub

    Public Sub ResumeRefresh()
        If _SuspendCount > 0 Then _SuspendCount -= 1
        If _SuspendCount > 0 Then Return
        If _Pending.Count = 0 Then Return
        If _FlushTimer Is Nothing Then
            _FlushTimer = New Windows.Threading.DispatcherTimer With {.Interval = TimeSpan.FromMilliseconds(90)}
            AddHandler _FlushTimer.Tick, AddressOf FlushPending
        End If
        _FlushTimer.Stop()
        _FlushTimer.Start()
    End Sub

    Public Sub GlassQueueRefresh(Panel As IGlassPanel)
        If Panel Is Nothing Then Return
        SyncLock _Pending
            For Each Ref As WeakReference In _Pending
                If Ref.Target IsNot Nothing AndAlso Ref.Target Is Panel Then Return
            Next
            _Pending.Add(New WeakReference(Panel))
        End SyncLock

        If _SuspendCount > 0 Then Return
        If _FlushTimer Is Nothing Then
            _FlushTimer = New Windows.Threading.DispatcherTimer With {.Interval = TimeSpan.FromMilliseconds(90)}
            AddHandler _FlushTimer.Tick, AddressOf FlushPending
        End If
        _FlushTimer.Stop()
        _FlushTimer.Start()
    End Sub

    Private Sub FlushPending(sender As Object, e As EventArgs)
        If _FlushTimer IsNot Nothing Then _FlushTimer.Stop()
        Dim Targets As New List(Of IGlassPanel)
        SyncLock _Pending
            For Each Ref As WeakReference In _Pending
                Dim Target As IGlassPanel = TryCast(Ref.Target, IGlassPanel)
                If Target IsNot Nothing Then Targets.Add(Target)
            Next
            _Pending.Clear()
        End SyncLock
        For Each Target As IGlassPanel In Targets
            Try
                Target.GlassRefreshBackdrop()
            Catch
            End Try
        Next
    End Sub

#End Region

#Region "弹窗"

    Public Sub GlassAttachDialog(Border As Border)
        If Border Is Nothing OrElse FrmMain Is Nothing Then Return
        Dim Content As UIElement = Border.Child
        If Content Is Nothing Then Return

        If TypeOf Content Is Grid AndAlso CType(Content, Grid).Tag Is GlassHostTag Then Return
        Try
            Dim Corner As Double = Math.Max(0, GlassCornerRadius)
            Dim Host As New Grid With {.Tag = GlassHostTag}
            Dim Backdrop As New Shapes.Rectangle With {.IsHitTestVisible = False, .RadiusX = Corner, .RadiusY = Corner}
            Dim Tint As New Shapes.Rectangle With {.Fill = GlassDialogTint(), .IsHitTestVisible = False, .RadiusX = Corner, .RadiusY = Corner}

            Border.Background = Nothing
            Border.CornerRadius = New CornerRadius(Corner)
            Border.BorderThickness = New Thickness(1)
            Border.BorderBrush = GlassEdgeBrush()

            Dim RawVisibility As Visibility = Border.Visibility
            Border.Visibility = Visibility.Collapsed

            Dim RawMask As Brush = Nothing
            Dim MaskTouched As Boolean = False
            Try
                If FrmMain.PanMsg IsNot Nothing Then
                    RawMask = FrmMain.PanMsg.Background
                    FrmMain.PanMsg.Background = New SolidColorBrush(Color.FromArgb(90, 0, 0, 0))
                    MaskTouched = True
                End If
            Catch
            End Try
            Try
                GlassInvalidateSnapshot()
                FrmMain.PanForm.UpdateLayout()
                Backdrop.Fill = GlassSurfaceBrush(Border, True)
            Finally
                Border.Visibility = RawVisibility
                If MaskTouched Then
                    Try
                        FrmMain.PanMsg.Background = RawMask
                    Catch
                    End Try
                End If
            End Try

            Host.Children.Add(Backdrop)
            Host.Children.Add(Tint)
            Border.Child = Nothing
            Host.Children.Add(Content)
            Border.Child = Host

            Dim Target As New GlassDialogPanel(Border, Backdrop)
            AddHandler Host.SizeChanged, Sub() GlassQueueRefresh(Target)
        Catch ex As Exception
            Logger.Info($"[Glass] 弹窗玻璃化失败：{ex.Message}")
        End Try
    End Sub

    Public Sub GlassRefreshDialogs()
        If FrmMain Is Nothing OrElse FrmMain.PanMsg Is Nothing Then Return
        Dim WantGlass As Boolean = Settings.Get(Of Integer)("UiUiStyle") = 1
        Dim Stack As New Stack(Of DependencyObject)
        Stack.Push(FrmMain.PanMsg)
        Do While Stack.Count > 0
            Dim Current As DependencyObject = Stack.Pop()
            Dim B As Border = TryCast(Current, Border)
            If B IsNot Nothing AndAlso B.Name = "PanBorder" Then
                If WantGlass Then
                    GlassAttachDialog(B)
                Else
                    GlassDetachDialog(B)
                End If
            End If
            For i As Integer = 0 To VisualTreeHelper.GetChildrenCount(Current) - 1
                Stack.Push(VisualTreeHelper.GetChild(Current, i))
            Next
        Loop
    End Sub

    Public Sub GlassDetachDialog(Border As Border)
        If Border Is Nothing Then Return
        Dim Host As Grid = TryCast(Border.Child, Grid)
        If Host Is Nothing OrElse Host.Tag IsNot GlassHostTag Then Return
        Try
            Dim Content As UIElement = Nothing
            For Each Child As UIElement In Host.Children
                If TypeOf Child Is Shapes.Rectangle Then Continue For
                Content = Child
            Next
            If Content Is Nothing Then Return
            Host.Children.Clear()
            Border.Child = Content
            Border.Background = New SolidColorBrush(Color.FromRgb(&HFB, &HFB, &HFB))
            Border.CornerRadius = New CornerRadius(7)
            Border.BorderThickness = New Thickness(0)
            Border.BorderBrush = Nothing
        Catch ex As Exception
            Logger.Warn(ex, "[Glass] 还原弹窗外观失败")
        End Try
    End Sub

#End Region

End Module
