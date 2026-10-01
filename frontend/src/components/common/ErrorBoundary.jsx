import React from 'react';
import { FiAlertTriangle, FiRefreshCw, FiHome } from 'react-icons/fi';

export class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, errorInfo) {
    console.error('ErrorBoundary caught an unhandled rendering error:', error, errorInfo);
  }

  handleReload = () => {
    this.setState({ hasError: false, error: null });
    window.location.reload();
  };

  handleGoHome = () => {
    this.setState({ hasError: false, error: null });
    window.location.href = '/';
  };

  render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen bg-theme-app text-theme-primary flex items-center justify-center p-6">
          <div className="max-w-md w-full bg-theme-surface rounded-2xl border border-theme shadow-2xl p-8 text-center">
            <div className="w-16 h-16 rounded-2xl bg-red-100 dark:bg-red-950/50 text-red-600 dark:text-red-400 flex items-center justify-center mx-auto mb-4 border border-red-200 dark:border-red-900/50">
              <FiAlertTriangle className="w-8 h-8" />
            </div>
            <h1 className="text-xl font-extrabold tracking-tight text-theme-primary mb-2">
              Something went wrong
            </h1>
            <p className="text-xs text-theme-secondary mb-6 leading-relaxed">
              An unexpected error occurred while rendering this view. Our diagnostic systems have recorded this issue.
            </p>
            {this.state.error?.message && (
              <div className="mb-6 p-3 bg-red-50/50 dark:bg-red-950/20 border border-red-200 dark:border-red-900/50 rounded-xl text-left text-xs font-mono text-red-700 dark:text-red-300 overflow-x-auto max-h-32">
                {this.state.error.message}
              </div>
            )}
            <div className="flex items-center justify-center gap-3">
              <button
                type="button"
                onClick={this.handleReload}
                className="inline-flex items-center gap-2 px-4 py-2 text-xs font-bold rounded-lg bg-theme-primary text-white hover:brightness-95 transition-all shadow-sm"
              >
                <FiRefreshCw className="w-3.5 h-3.5" /> Reload Page
              </button>
              <button
                type="button"
                onClick={this.handleGoHome}
                className="inline-flex items-center gap-2 px-4 py-2 text-xs font-semibold rounded-lg border border-theme bg-theme-elevated text-theme-primary hover:bg-theme-surface transition-all"
              >
                <FiHome className="w-3.5 h-3.5" /> Return Home
              </button>
            </div>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;
