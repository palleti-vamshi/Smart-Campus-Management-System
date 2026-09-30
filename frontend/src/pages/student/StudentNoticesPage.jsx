import React, { useState, useEffect } from 'react';
import { noticeService } from '../../services/noticeService';
import PageHeader from '../../components/common/PageHeader';
import StatusBadge from '../../components/common/Badge';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/common/ErrorState';
import EmptyState from '../../components/common/EmptyState';
import SearchBar from '../../components/common/SearchBar';
import { FiCalendar, FiUser, FiAlertTriangle, FiBell } from 'react-icons/fi';
import { formatDateDDMMYYYY } from '../../utils/academicCalendar';

const CATEGORIES = ['ALL', 'ACADEMIC', 'EXAM', 'EVENT', 'INTERNSHIP', 'PLACEMENT', 'IMPORTANT', 'GENERAL'];
const PRIORITIES = ['ALL', 'URGENT', 'HIGH', 'NORMAL', 'LOW'];

export const StudentNoticesPage = () => {
  const [notices, setNotices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedCategory, setSelectedCategory] = useState('ALL');
  const [selectedPriority, setSelectedPriority] = useState('ALL');
  const [searchTerm, setSearchTerm] = useState('');

  const fetchNotices = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await noticeService.getMyNotices();
      const rawList = res?.data?.content || res?.content || res?.data || res || [];
      const list = Array.isArray(rawList) ? rawList : [];
      setNotices(list);
    } catch (err) {
      setError(err.friendlyMessage || err.response?.data?.message || err.message || 'Failed to load notices.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchNotices();
  }, []);

  const safeNotices = Array.isArray(notices) ? notices : [];
  const filteredNotices = safeNotices.filter((n) => {
    const matchCat = selectedCategory === 'ALL' || n.category === selectedCategory;
    const matchPriority = selectedPriority === 'ALL' || n.priority === selectedPriority;
    const matchSearch =
      !searchTerm ||
      (n.title && n.title.toLowerCase().includes(searchTerm.toLowerCase())) ||
      (n.content && n.content.toLowerCase().includes(searchTerm.toLowerCase()));
    return matchCat && matchPriority && matchSearch;
  });

  return (
    <div className="space-y-6">
      <PageHeader
        title="Notice Board"
        subtitle="Department announcements, examination notices, and internship postings"
      />

      {/* Filter and Search Bar */}
      <div className="bg-theme-surface p-4 rounded-xl border border-theme shadow-sm space-y-3.5">
        <div className="flex flex-col sm:flex-row gap-4 justify-between items-start sm:items-center">
          {/* Category Chips */}
          <div className="flex flex-wrap gap-1.5">
            {CATEGORIES.map((cat) => (
              <button
                key={cat}
                onClick={() => setSelectedCategory(cat)}
                className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                  selectedCategory === cat
                    ? 'bg-theme-primary text-white shadow-xs brightness-100'
                    : 'bg-theme-elevated text-theme-secondary border border-theme hover:border-theme-primary'
                }`}
              >
                {cat}
              </button>
            ))}
          </div>

          {/* Search input */}
          <div className="w-full sm:w-64 shrink-0">
            <SearchBar
              value={searchTerm}
              onChange={setSearchTerm}
              placeholder="Search announcements..."
            />
          </div>
        </div>

        {/* Priority Filter */}
        <div className="flex items-center gap-2 pt-2 border-t border-theme-subtle">
          <span className="text-xs font-semibold text-theme-muted uppercase tracking-wider">Priority:</span>
          <div className="flex flex-wrap gap-1.5">
            {PRIORITIES.map((p) => (
              <button
                key={p}
                onClick={() => setSelectedPriority(p)}
                className={`px-2.5 py-1 rounded-md text-xs font-medium transition-all ${
                  selectedPriority === p
                    ? 'bg-theme-primary-light text-theme-brand font-bold border border-theme'
                    : 'text-theme-secondary hover:text-theme-primary'
                }`}
              >
                {p}
              </button>
            ))}
          </div>
        </div>
      </div>

      {loading && <LoadingSpinner message="Fetching department notices..." />}
      {error && <ErrorState message={error} onRetry={fetchNotices} />}

      {!loading && !error && filteredNotices.length === 0 && (
        <EmptyState
          icon={FiBell}
          title="No notices matching the criteria"
          description="Try broadening your category or priority filter."
        />
      )}

      {!loading && !error && filteredNotices.length > 0 && (
        <div className="space-y-4">
          {filteredNotices.map((n) => {
            const isUrgent = n.priority === 'URGENT';
            const isHigh = n.priority === 'HIGH' || n.category === 'IMPORTANT';

            return (
              <div
                key={n.noticeId}
                className={`bg-theme-surface rounded-xl border p-5 shadow-sm transition-all duration-150 hover:shadow-md ${
                  isUrgent
                    ? 'border-l-4 border-l-red-500 border-red-200 dark:border-red-900/60 bg-red-50/15 dark:bg-red-950/20'
                    : isHigh
                    ? 'border-l-4 border-l-amber-500 border-theme'
                    : 'border-theme'
                }`}
              >
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 pb-3 border-b border-theme-subtle">
                  <div className="flex items-center gap-2 flex-wrap">
                    {isUrgent && (
                      <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-extrabold bg-red-600 text-white animate-pulse">
                        <FiAlertTriangle className="w-3 h-3" /> URGENT
                      </span>
                    )}
                    <h2 className="text-base font-bold text-theme-primary">{n.title}</h2>
                    <StatusBadge status={n.category} />
                    <StatusBadge status={n.priority} />
                  </div>
                  <div className="flex items-center gap-3 text-xs text-theme-muted">
                    <span className="flex items-center gap-1">
                      <FiCalendar className="w-3.5 h-3.5" />
                      Published: {formatDateDDMMYYYY(n.publishedAt || n.publishDate)}
                    </span>
                    {n.expiryDate && (
                      <span className="hidden sm:inline">Expires: {formatDateDDMMYYYY(n.expiryDate)}</span>
                    )}
                  </div>
                </div>

                <p className="mt-3 text-sm text-theme-primary leading-relaxed whitespace-pre-line font-normal">
                  {n.content}
                </p>

                <div className="mt-4 pt-3 border-t border-theme-subtle flex items-center justify-between text-xs text-theme-muted">
                  <span className="flex items-center gap-1.5 font-medium">
                    <FiUser className="w-3.5 h-3.5" />
                    Target: {n.targetProgramCode || n.targetProgram || 'All Department Programs'}
                  </span>
                  {n.authorName && (
                    <span className="font-semibold text-theme-secondary">
                      Posted by: {n.authorName}
                    </span>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};

export default StudentNoticesPage;
